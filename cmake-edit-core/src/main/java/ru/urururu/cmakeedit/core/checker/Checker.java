package ru.urururu.cmakeedit.core.checker;

import com.codahale.metrics.Timer;
import ru.urururu.cmakeedit.core.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by okutane on 11/08/16.
 */
public class Checker {
    static final Map<String, Function<CommandInvocationNode, Node>> setters =
            new HashMap<String, Function<CommandInvocationNode, Node>>() {{
                put("set", cmd -> cmd.getArguments().get(0));
                put("list", cmd -> Arrays.asList("LENGTH", "GET", "FIND").contains(((ArgumentNode) cmd.getArguments().get(0)).getArgument()) ? cmd.getArguments().get(cmd.getArguments().size() - 1) : null);
            }};

    public static void findUnused(CheckContext ctx) throws LogicalException {
        try (Timer.Context time = ctx.getRegistry().timer(name(Checker.class, "findUnused")).time()) {
            FileNode ast = ctx.getAst();

            List<CommandInvocationNode> nodes =
                    ast.getNodes().stream()
                            .filter(n -> n instanceof CommandInvocationNode)
                            .map(n -> (CommandInvocationNode) n)
                            .collect(Collectors.toList());

            Set<Node> suspiciousPoints = new LinkedHashSet<>();
            ast.visitAll(new NodeVisitorAdapter() {
                @Override
                public void accept(CommandInvocationNode node) {
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
                        simulate(ctx, new SimulationState(macroBody, 0));

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
                        }, new SimulationState(functionBody, 0));

                        return functionSimulator.simulate(ctx, state, command);
                    });
                }

                @Override
                protected void process(SimulationState state, CommandInvocationNode node) {
                    state.simulate(suspiciousPoints, state.getCurrent());
                    super.process(state, node);
                }
            };

            unusedSimulator.simulate(ctx, new SimulationState(nodes, 0));

            suspiciousPoints.forEach(n -> ctx.getReporter().report(new SourceRange(n.getStart(), n.getEnd()), "Value not used"));
        }
    }
}
