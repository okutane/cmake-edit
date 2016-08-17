package ru.urururu.cmakeedit.core.checker;

import com.codahale.metrics.Timer;
import ru.urururu.cmakeedit.core.CommandInvocationNode;
import ru.urururu.cmakeedit.core.Node;

import java.util.*;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by okutane on 16/08/16.
 */
public class AbstractSimulator {
    boolean explicit = false;
    static final List<String> builtins = Arrays.asList("include_directories", "set", "unset", "if", "message");
    private Set<Node> suspiciousPoints;

    Map<String, CommandSimulator> simulators = new HashMap<>();

    public AbstractSimulator(Set<Node> suspiciousPoints) {
        this.suspiciousPoints = suspiciousPoints;

        Map<String, CommandSimulator> simulators = new HashMap<>();
        init(simulators);
        this.simulators = new HashMap<>(simulators);
    }

    protected void init(Map<String, CommandSimulator> simulators) {
        simulators.put("function", (ctx, state, cmd) -> {
            LogicalBlock logicalBlock = LogicalBlockFinder.find(state.getNodes(), state.getPosition(), "endfunction");
            state.setPosition(logicalBlock.endPosition);
        });
        simulators.put("macro", (ctx, state, cmd) -> {
            LogicalBlock logicalBlock = LogicalBlockFinder.find(state.getNodes(), state.getPosition(), "endmacro");
            state.setPosition(logicalBlock.endPosition);
        });
        simulators.put("foreach", (ctx, state, cmd) -> {
            LogicalBlock logicalBlock = LogicalBlockFinder.find(state.getNodes(), state.getPosition(), "endforeach");

            List<CommandInvocationNode> foreachBody = logicalBlock.bodies.get(0);

            state.setPosition(logicalBlock.endPosition);
        });
        simulators.put("while", (ctx, state, cmd) -> {
            LogicalBlock logicalBlock = LogicalBlockFinder.find(state.getNodes(), state.getPosition(), "endwhile");

            List<CommandInvocationNode> whileBody = logicalBlock.bodies.get(0);

            state.setPosition(logicalBlock.endPosition);
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

    void simulate(CheckContext ctx, SimulationState state) throws LogicalException {
        try (Timer.Context simulateTime = ctx.getRegistry().timer(name(getClass(), "simulate")).time()) {
            while (state.getPosition() < state.getNodes().size()) {
                CommandInvocationNode current = state.getNodes().get(state.getPosition());

                String commandName = current.getCommandName();
                CommandSimulator simulator = simulators.get(commandName);
                if (simulator != null) {
                    simulator.simulate(ctx, state, current);
                } else if (commandName.equals("if")) {
                    try (Timer.Context processTime = ctx.getRegistry().timer(name(getClass(), "process", commandName)).time()) {
                        LogicalBlock branches = LogicalBlockFinder.findIfNodes(state.getNodes(), state.getPosition());

                        for (CommandInvocationNode header : branches.headers) {
                            state.simulate(suspiciousPoints, header);
                        }

                        List<SimulationState> newStates = new ArrayList<>();
                        for (List<CommandInvocationNode> branch : branches.bodies) {
                            SimulationState newState = new SimulationState(branch, 0, new LinkedHashMap<>(state.getVariables()));
                            simulate(ctx, newState);
                            newStates.add(newState);
                        }

                        state = merge(newStates, state.getNodes(), branches.endPosition);
                    }
                } else if (!builtins.contains(commandName) && explicit) {
                    throw new IllegalStateException(commandName);
                } else {
                    process(state, current);
                }
            }
        }
    }

    protected void process(SimulationState state, CommandInvocationNode node) {
        advance(state);
    }

    private void advance(SimulationState state) {
        state.setPosition(state.getPosition() + 1);
    }

    private SimulationState merge(List<SimulationState> newStates, List<CommandInvocationNode> nodes, int mergePoint) {
        Map<String, Set<CommandInvocationNode>> variables = new HashMap<>();
        for (SimulationState newState : newStates) {
            for (Map.Entry<String, Set<CommandInvocationNode>> variable : newState.getVariables().entrySet()) {
                variables.computeIfAbsent(variable.getKey(), key -> new HashSet<>()).addAll(variable.getValue());
            }
        }

        return new SimulationState(nodes, mergePoint, variables);
    }

    interface CommandSimulator {
        void simulate(CheckContext ctx, SimulationState state, CommandInvocationNode command) throws LogicalException;
    }
}
