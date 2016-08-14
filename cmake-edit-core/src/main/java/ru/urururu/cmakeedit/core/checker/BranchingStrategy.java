package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.CommandInvocationNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by okutane on 16/08/16.
 */
public class BranchingStrategy {
    BranchingInfo getBranches(SimulationState state) throws LogicalException {
        CommandInvocationNode commandInvocationNode = state.getCurrent();
        if (commandInvocationNode.getCommandName().equalsIgnoreCase("if")) {
            return getIfBranches(commandInvocationNode, state);
        }

        return getTrivialBranch(commandInvocationNode, state);
    }

    protected BranchingInfo getIfBranches(CommandInvocationNode commandInvocationNode, SimulationState state) throws LogicalException {
        BranchingInfo result = new BranchingInfo();
        result.branches = new ArrayList<>();

        int position = state.getPosition();
        List<CommandInvocationNode> nodes = state.getNodes();

        int depth = 0;
        boolean elseEncountered = false;

        int from = position + 1;

        for (int i = from; i < nodes.size(); i++) {
            CommandInvocationNode futureCommand = nodes.get(i);
            switch (futureCommand.getCommandName().toLowerCase()) {
                case "if":
                    depth++;
                    continue;
                case "elseif":
                    if (depth == 0) {
                        // processUsages(futureCommand); // todo its important to process
                        result.branches.add(nodes.subList(from, i));
                        from = i + 1;
                    }
                    continue;
                case "else":
                    if (depth == 0) {
                        if (elseEncountered) {
                            continue;
                        }
                        elseEncountered = true;

                        result.branches.add(nodes.subList(from, i));
                        from = i + 1;
                    }
                    continue;
                case "endif":
                    if (depth == 0) {
                        result.branches.add(nodes.subList(from, i));
                        if (!elseEncountered) {
                            result.branches.add(Collections.emptyList());
                        }
                        result.mergePoint = i + 1;
                        return result;
                    }
                    depth--;
            }
        }

        throw new LogicalException("A logical block is not closed", commandInvocationNode, nodes.get(nodes.size() - 1));
    }

    private BranchingInfo getTrivialBranch(CommandInvocationNode node, SimulationState state) {
        BranchingInfo result = new BranchingInfo();
        result.branches = Collections.singletonList(Collections.singletonList(node));
        result.mergePoint = state.getPosition() + 1;

        return result;
    }
}
