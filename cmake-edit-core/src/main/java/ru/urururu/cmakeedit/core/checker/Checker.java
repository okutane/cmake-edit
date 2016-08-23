package ru.urururu.cmakeedit.core.checker;

import com.codahale.metrics.Timer;
import ru.urururu.cmakeedit.core.*;

import java.util.*;
import java.util.function.Function;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by okutane on 11/08/16.
 */
public class Checker {
    static boolean STRICT_MODE = false;

    static final Map<String, Function<CommandInvocationNode, Node>> setters =
            new HashMap<String, Function<CommandInvocationNode, Node>>() {{
                put("set", cmd -> {
                    if (cmd.getArguments().size() > 2 && ((ArgumentNode)cmd.getArguments().get(cmd.getArguments().size() - 1)).getArgument().equals("PARENT_SCOPE")){
                        return null;
                    }
                    return cmd.getArguments().get(0);
                });
                put("list", cmd -> {
                    if (Arrays.asList("LENGTH", "GET", "FIND").contains(((ArgumentNode) cmd.getArguments().get(0)).getArgument()))
                        return cmd.getArguments().get(cmd.getArguments().size() - 1);
                    return null;
                });
            }};

    public static void findUnused(CheckContext ctx) throws LogicalException {
        try (Timer.Context time = ctx.getRegistry().timer(name(Checker.class, "findUnused")).time()) {
            FileNode ast = ctx.getAst();

            List<CommandInvocationNode> nodes = new ArrayList<>();
            Set<CommandInvocationNode> suspiciousPoints = new LinkedHashSet<>();

            ast.visitAll(new NodeVisitorAdapter() {
                @Override
                public void accept(CommandInvocationNode node) {
                    nodes.add(node);

                    Function<CommandInvocationNode, Node> setter = setters.get(node.getCommandName());

                    if (setter == null) {
                        return;
                    }

                    ArgumentNode argument = (ArgumentNode) setter.apply(node);

                    if (argument == null || argument.getArgument().startsWith("CMAKE_")) {
                        return;
                    }

                    suspiciousPoints.add(node);
                }
            });

            AbstractSimulator unusedSimulator = new AbstractSimulator(suspiciousPoints) {
                @Override
                protected void init(Map<String, CommandSimulator> simulators) {
                    super.init(simulators);

                    CommandSimulator macroSimulator = simulators.get("macro");
                    simulators.put("macro", (ctx, state, command) -> {
                        LogicalBlock logicalBlock = LogicalBlockFinder.find(state.getNodes(), state.getPosition(), "endmacro");

                        List<CommandInvocationNode> macroBody = logicalBlock.bodies.get(0);
                        simulate(ctx, new SimulationState(macroBody, 0, suspiciousPoints)); // todo maybe we shouldn't simulate macroes at this point?

                        return macroSimulator.simulate(ctx, state, command);
                    });

                    CommandSimulator functionSimulator = simulators.get("function");
                    simulators.put("function", (ctx, state, command) -> {
                        LogicalBlock logicalBlock = LogicalBlockFinder.find(state.getNodes(), state.getPosition(), "endfunction");

                        List<CommandInvocationNode> functionBody = logicalBlock.bodies.get(0);
                        List<SimulationState> ignored = new ArrayList<>();
                        simulate(new CheckContextDecorator(ctx) {
                            @Override
                            public List<SimulationState> getFunctionStates() {
                                return ignored;
                            }

                            @Override
                            public List<SimulationState> getLoopStates() {
                                return null;
                            }
                        }, new SimulationState(functionBody, 0, suspiciousPoints));

                        return functionSimulator.simulate(ctx, state, command);
                    });

                    simulators.put("set", new CommandSimulator() {
                        @Override
                        public SimulationState simulate(CheckContext ctx, SimulationState state, CommandInvocationNode command) throws LogicalException {
                            List<Node> arguments = command.getArguments();

                            Iterator<Node> iterator = arguments.iterator();

                            ArgumentNode variable;
                            if (iterator.hasNext()) {
                                variable = (ArgumentNode) iterator.next();
                            } else {
                                throw new LogicalException("wrong number of arguments", command, command);
                            }

                            String lastValue = null;
                            while (iterator.hasNext()) {
                                lastValue = state.getValue((ArgumentNode) iterator.next());
                            }

                            boolean parentScope = "PARENT_SCOPE".equals(lastValue);

                            state.putValue(variable, command, parentScope);

                            state.setPosition(state.getPosition() + 1);
                            return state;
                        }
                    });

                    simulators.put("list", new CommandSimulator() {
                        List<String> setterOperations = Arrays.asList("LENGTH", "GET", "FIND");

                        @Override
                        public SimulationState simulate(CheckContext ctx, SimulationState state, CommandInvocationNode command) throws LogicalException {
                            List<Node> arguments = command.getArguments();
                            String operation = state.getValue((ArgumentNode) arguments.get(0));

                            for (int i = 1; i < arguments.size() - 1; i++) {
                                // use every argument
                                state.getValue((ArgumentNode) arguments.get(i));
                            }

                            ArgumentNode lastArgument = (ArgumentNode) arguments.get(arguments.size() - 1);
                            if (setterOperations.contains(operation)) {
                                state.putValue(lastArgument, command);
                            } else {
                                // use last argument
                                state.getValue(lastArgument);
                            }

                            state.setPosition(state.getPosition() + 1);
                            return state;
                        }
                    });
                }

                @Override
                protected void process(SimulationState state, CommandInvocationNode node) throws LogicalException {
                    String commandName = node.getCommandName();
                    if (!Builtins.NORMAL.contains(commandName) && STRICT_MODE) {
                        throw new LogicalException("Unknown CMake command '" + commandName + "'", node, node);
                    }

                    state.simulate(suspiciousPoints, state.getCurrent());
                    super.process(state, node);
                }
            };

            unusedSimulator.simulate(ctx, new SimulationState(nodes, 0, suspiciousPoints));

            suspiciousPoints.forEach(n -> ctx.getReporter().report(new SourceRange(n.getStart(), n.getEnd()), "Value not used"));
        }
    }
}
