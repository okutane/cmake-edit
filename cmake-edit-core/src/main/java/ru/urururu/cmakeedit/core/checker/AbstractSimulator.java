package ru.urururu.cmakeedit.core.checker;

import com.codahale.metrics.Timer;
import ru.urururu.cmakeedit.core.ArgumentNode;
import ru.urururu.cmakeedit.core.CommandInvocationNode;
import ru.urururu.cmakeedit.core.Node;

import java.util.*;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by okutane on 16/08/16.
 */
class AbstractSimulator {
    private Set<Node> suspiciousPoints;

    private Map<String, CommandSimulator> staticSimulators = new HashMap<>();

    AbstractSimulator(Set<Node> suspiciousPoints) {
        this.suspiciousPoints = suspiciousPoints;

        Map<String, CommandSimulator> simulators = new HashMap<>();
        init(simulators);
        this.staticSimulators = new HashMap<>(simulators);
    }

    protected void init(Map<String, CommandSimulator> simulators) {
        simulators.put("function", (ctx, state, cmd) -> {
            LogicalBlock logicalBlock = LogicalBlockFinder.find(state.getNodes(), state.getPosition(), "endfunction");

            ArgumentNode node = (ArgumentNode) cmd.getArguments().get(0);
            state.addSimulator(node.getArgument(), new FunctionSimulator(this, logicalBlock.bodies.get(0)));

            state.setPosition(logicalBlock.endPosition);
            return state;
        });
        simulators.put("return", (ctx, state, cmd) -> {
            List<SimulationState> functionStates = ctx.getFunctionStates();
            functionStates.add(state);
            return null;
        });

        simulators.put("macro", (ctx, state, cmd) -> {
            LogicalBlock logicalBlock = LogicalBlockFinder.find(state.getNodes(), state.getPosition(), "endmacro");

            ArgumentNode node = (ArgumentNode) cmd.getArguments().get(0);
            state.addSimulator(node.getArgument(), new MacroSimulator(this, logicalBlock.bodies.get(0)));

            state.setPosition(logicalBlock.endPosition);
            return state;
        });

        simulators.put("if", (ctx, state, cmd) -> {
            LogicalBlock branches = LogicalBlockFinder.findIfNodes(state.getNodes(), state.getPosition());

            for (CommandInvocationNode header : branches.headers) {
                state.simulate(suspiciousPoints, header);
            }

            List<SimulationState> newStates = new ArrayList<>();
            for (List<CommandInvocationNode> branch : branches.bodies) {
                SimulationState newState = simulate(ctx, new SimulationState(branch, 0, new LinkedHashMap<>(state.getVariables())));
                if (newState != null) {
                    newStates.add(newState);
                }
            }

            return merge(newStates, state.getNodes(), branches.endPosition);
        });

        simulators.put("foreach", (ctx, state, cmd) -> {
            LogicalBlock logicalBlock = LogicalBlockFinder.find(state.getNodes(), state.getPosition(), "endforeach");
            return simulateLoop(ctx, state, logicalBlock);
        });
        simulators.put("while", (ctx, state, cmd) -> {
            LogicalBlock logicalBlock = LogicalBlockFinder.find(state.getNodes(), state.getPosition(), "endwhile");
            return simulateLoop(ctx, state, logicalBlock);
        });
        simulators.put("break", (ctx, state, cmd) -> {
            List<SimulationState> loopStates = ctx.getLoopStates();
            if (loopStates == null) {
                throw new UnexpectedCommandException(cmd);
            }

            loopStates.add(state);
            return null;
        });

        simulators.put("endforeach", (ctx, state, cmd) -> {
            throw new UnexpectedCommandException(cmd);
        });
        simulators.put("endfunction", (ctx, state, cmd) -> {
            throw new UnexpectedCommandException(cmd);
        });
        simulators.put("endif", (ctx, state, cmd) -> {
            throw new UnexpectedCommandException(cmd);
        });
        simulators.put("endmacro", (ctx, state, cmd) -> {
            throw new UnexpectedCommandException(cmd);
        });
        simulators.put("endwhile", (ctx, state, cmd) -> {
            throw new UnexpectedCommandException(cmd);
        });
    }

    private SimulationState simulateLoop(CheckContext ctx, SimulationState state, LogicalBlock logicalBlock) throws LogicalException {
        for (CommandInvocationNode header : logicalBlock.headers) {
            state.simulate(suspiciousPoints, header);
        }

        List<SimulationState> loopStates = new ArrayList<>();
        CheckContext loopCtx = new CheckContextDecorator(ctx) {
            @Override
            public List<SimulationState> getLoopStates() {
                return loopStates;
            }
        };

        for (List<CommandInvocationNode> branch : logicalBlock.bodies) {
            SimulationState newState = simulate(loopCtx, new SimulationState(branch, 0, new LinkedHashMap<>(state.getVariables())));
            if (newState != null) {
                loopStates.add(newState);
            }
        }
        loopStates.add(state); // case when we don't enter loop

        return merge(loopStates, state.getNodes(), logicalBlock.endPosition);
    }

    SimulationState simulate(CheckContext ctx, SimulationState state) throws LogicalException {
        try (Timer.Context simulateTime = ctx.getRegistry().timer(name(getClass(), "simulate")).time()) {
            while (state != null && state.getPosition() < state.getNodes().size()) {
                CommandInvocationNode current = state.getNodes().get(state.getPosition());

                String commandName = current.getCommandName();

                CommandSimulator dynamicSimulator = state.getSimulator(commandName);
                if (dynamicSimulator != null) {
                    state = dynamicSimulator.simulate(ctx, state, current);
                }

                CommandSimulator simulator = staticSimulators.get(commandName);
                if (simulator != null) {
                    try (Timer.Context processTime = ctx.getRegistry().timer(name(getClass(), "process", commandName)).time()) {
                        state = simulator.simulate(ctx, state, current);
                    }
                } else {
                    process(state, current);
                }
            }
            return state;
        }
    }

    protected void process(SimulationState state, CommandInvocationNode node) throws LogicalException {
        advance(state);
    }

    private void advance(SimulationState state) {
        state.setPosition(state.getPosition() + 1);
    }

    public SimulationState merge(List<SimulationState> newStates, List<CommandInvocationNode> nodes, int mergePoint) {
        if (newStates.isEmpty()) {
            return null;
        }

        Map<String, Set<CommandInvocationNode>> variables = new HashMap<>();
        for (SimulationState newState : newStates) {
            for (Map.Entry<String, Set<CommandInvocationNode>> variable : newState.getVariables().entrySet()) {
                variables.computeIfAbsent(variable.getKey(), key -> new HashSet<>()).addAll(variable.getValue());
            }
        }

        return new SimulationState(nodes, mergePoint, variables);
    }

    interface CommandSimulator {
        SimulationState simulate(CheckContext ctx, SimulationState state, CommandInvocationNode command) throws LogicalException;
    }
}
