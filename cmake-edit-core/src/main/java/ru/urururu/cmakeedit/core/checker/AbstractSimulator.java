package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.CommandInvocationNode;

import java.util.*;

/**
 * Created by okutane on 16/08/16.
 */
public class AbstractSimulator {
    private BranchingStrategy branchingStrategy = new BranchingStrategy();

    void simulate(SimulationState state) throws LogicalException {
        while (state.getPosition() < state.getNodes().size()) {
            CommandInvocationNode current = state.getNodes().get(state.getPosition());

            if (current.getCommandName().equalsIgnoreCase("if")) {
                BranchingInfo branches = branchingStrategy.getIfBranches(current, state);

                List<SimulationState> newStates = new ArrayList<>();
                for (List<CommandInvocationNode> branch : branches.branches) {
                    SimulationState newState = new SimulationState(branch, 0, new LinkedHashMap<>(state.getVariables()));
                    simulate(newState);
                    newStates.add(newState);
                }

                state = merge(newStates, state.getNodes(), branches.mergePoint);
            } else {
                process(state, current);
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
