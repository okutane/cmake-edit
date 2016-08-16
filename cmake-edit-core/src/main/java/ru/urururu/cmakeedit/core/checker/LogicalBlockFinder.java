package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.CommandInvocationNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by okutane on 16/08/16.
 */
public class LogicalBlockFinder {
    public static LogicalBlock find(List<CommandInvocationNode> nodes, int start, String endName, String... separatorNames) throws LogicalException {
        LogicalBlock result = new LogicalBlock();

        CommandInvocationNode startNode = nodes.get(start);
        String startName = startNode.getCommandName();

        result.headers.add(startNode);
        int from = start + 1;
        int depth = 0;

        for (int i = from; i < nodes.size(); i++) {
            CommandInvocationNode futureCommand = nodes.get(i);
            if (futureCommand.getCommandName().equals(startName)) {
                depth++;
            } else if (futureCommand.getCommandName().equals(endName)) {
                if (depth-- == 0) {
                    // finish last block
                    result.bodies.add(nodes.subList(from, i));
                    result.endPosition = i + 1;
                    return result;
                }
            } else if (depth == 0 && Arrays.asList(separatorNames).contains(futureCommand.getCommandName())) {
                // finish previous block
                result.bodies.add(nodes.subList(from, i));

                // start new block
                result.headers.add(futureCommand);
                from = i + 1;
            }
        }

        throw new LogicalException("A logical block is not closed", startNode, nodes.get(nodes.size() - 1));
    }

    public static LogicalBlock findIfNodes(List<CommandInvocationNode> nodes, int start) throws LogicalException {
        LogicalBlock result = find(nodes, start, "endif", "elseif", "else");

        if (!result.headers.stream().anyMatch(n -> n.getCommandName().equals("else"))) {
            result.bodies.add(Collections.emptyList());
        }

        return result;
    }
}
