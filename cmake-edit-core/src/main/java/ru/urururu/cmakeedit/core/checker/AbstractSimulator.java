package ru.urururu.cmakeedit.core.checker;

import com.codahale.metrics.Timer;
import ru.urururu.cmakeedit.core.ArgumentNode;
import ru.urururu.cmakeedit.core.CommandInvocationNode;

import java.util.*;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by okutane on 16/08/16.
 */
class AbstractSimulator {
    private Map<String, CommandSimulator> staticSimulators = new HashMap<>();

    AbstractSimulator() {
        Map<String, CommandSimulator> simulators = new HashMap<>();
        init(simulators);
        this.staticSimulators = new HashMap<>(simulators);
    }

    protected void init(Map<String, CommandSimulator> simulators) {
        simulators.put("function", (ctx, state, cmd) -> {
            LogicalBlock logicalBlock = LogicalBlockFinder.find(state.getNodes(), state.getPosition(), "endfunction");

            ArgumentNode node = (ArgumentNode) cmd.getArguments().get(0);

            String name = state.getValue(node);
            List<String> formalParameters = cmd.getArguments().subList(1, cmd.getArguments().size())
                    .stream().map(state::getValue).collect(Collectors.toList());

            state.addSimulator(name, new FunctionSimulator(this, formalParameters, logicalBlock.bodies.get(0)));

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
            state.addSimulator(SimulationState.getArgument(node), new MacroSimulator(this, logicalBlock.bodies.get(0)));

            state.setPosition(logicalBlock.endPosition);
            return state;
        });

        simulators.put("if", (ctx, state, cmd) -> {
            LogicalBlock branches = LogicalBlockFinder.findIfNodes(state.getNodes(), state.getPosition());

            for (CommandInvocationNode header : branches.headers) {
                state.simulate(header);
            }

            List<SimulationState> newStates = new ArrayList<>();
            for (List<CommandInvocationNode> branch : branches.bodies) {
                SimulationState newState = simulate(ctx, state.copyAt(branch, 0));
                if (newState != null) {
                    newStates.add(newState);
                }
            }

            return state.merge(state.getNodes(), branches.endPosition, newStates);
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
            state.simulate(header);
        }

        List<SimulationState> loopStates = new ArrayList<>();
        CheckContext loopCtx = new CheckContextDecorator(ctx) {
            @Override
            public List<SimulationState> getLoopStates() {
                return loopStates;
            }
        };

        for (List<CommandInvocationNode> branch : logicalBlock.bodies) {
            SimulationState newState = simulate(loopCtx, state.copyAt(branch, 0));
            if (newState != null) {
                loopStates.add(newState);
            }
        }
        loopStates.add(state); // case when we don't enter loop

        return state.merge(state.getNodes(), logicalBlock.endPosition, loopStates);
    }

    SimulationState simulate(CheckContext ctx, SimulationState state) throws LogicalException {
        try (Timer.Context simulateTime = ctx.getRegistry().timer(name(getClass(), "simulate")).time()) {
            while (state != null && state.getPosition() < state.getNodes().size()) {
                CommandInvocationNode current = state.getNodes().get(state.getPosition());

                String commandName = current.getCommandName();

                CommandSimulator dynamicSimulator = state.getSimulator(commandName);
                if (dynamicSimulator != null) {
                    state = dynamicSimulator.simulate(ctx, state, current);
                } else {
                    CommandSimulator simulator = staticSimulators.get(commandName);
                    if (simulator != null) {
                        try (Timer.Context processTime = ctx.getRegistry().timer(name(getClass(), "process", commandName)).time()) {
                            state = simulator.simulate(ctx, state, current);
                        }
                    } else {
                        process(state, current);
                    }
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

    interface CommandSimulator {
        SimulationState simulate(CheckContext ctx, SimulationState state, CommandInvocationNode command) throws LogicalException;
    }
}
