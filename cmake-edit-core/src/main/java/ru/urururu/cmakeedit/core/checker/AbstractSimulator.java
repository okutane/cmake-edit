package ru.urururu.cmakeedit.core.checker;

import com.codahale.metrics.Timer;
import ru.urururu.cmakeedit.core.CommandInvocationNode;

import java.util.*;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by okutane on 16/08/16.
 */
public class AbstractSimulator {
    void simulate(CheckContext ctx, SimulationState state) throws LogicalException {
        try (Timer.Context simulateTime = ctx.getRegistry().timer(name(getClass(), "simulate")).time()) {
            while (state.getPosition() < state.getNodes().size()) {
                CommandInvocationNode current = state.getNodes().get(state.getPosition());

                    if (current.getCommandName().equals("if")) {
                        try (Timer.Context processTime = ctx.getRegistry().timer(name(getClass(), "process", current.getCommandName())).time()) {
                            LogicalBlock branches = LogicalBlockFinder.findIfNodes(state.getNodes(), state.getPosition());

                            List<SimulationState> newStates = new ArrayList<>();
                            for (List<CommandInvocationNode> branch : branches.bodies) {
                                SimulationState newState = new SimulationState(branch, 0, new LinkedHashMap<>(state.getVariables()));
                                simulate(ctx, newState);
                                newStates.add(newState);
                            }

                            state = merge(newStates, state.getNodes(), branches.endPosition);
                        }
                    } else {
                        process(state, current);
                    }
                }
        }
    }

    protected void process(SimulationState state, CommandInvocationNode node) {
        state.setPosition(state.getPosition() + 1);
    }

    private SimulationState merge(List<SimulationState> newStates, List<CommandInvocationNode> nodes, int mergePoint) {
        Map<String, Set<CommandInvocationNode>> variables = new HashMap<>();
        for (SimulationState newState : newStates) {
            for (Map.Entry<String, Set<CommandInvocationNode>> variable : newState.getVariables().entrySet()) {
                variables.computeIfAbsent(variable.getKey(), key -> new HashSet<>()).addAll(variable.getValue());
            }
        }

        SimulationState newState = newStates.get(0);
        return new SimulationState(nodes, mergePoint, variables);
    }
}
