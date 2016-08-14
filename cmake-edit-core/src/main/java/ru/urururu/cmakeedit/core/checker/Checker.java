package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by okutane on 11/08/16.
 */
public class Checker {
    public static void findUnused(FileNode ast, ProblemReporter reporter) throws LogicalException {
        List<CommandInvocationNode> nodes =
                ast.getNodes().stream()
                        .filter(n -> n instanceof CommandInvocationNode)
                        .map(n -> (CommandInvocationNode)n)
                        .collect(Collectors.toList());

        Set<Node> suspiciousPoints = new LinkedHashSet<>();
        ast.visitAll(new NodeVisitorAdapter() {
            @Override
            public void accept(CommandInvocationNode node) {
                if (!node.getCommandName().equalsIgnoreCase("set")) {
                    return;
                }

                if (node.getArguments().isEmpty()) {
                    // strange stuff
                    return;
                }

                ArgumentNode first = (ArgumentNode) node.getArguments().get(0);
                if (first.getArgument().startsWith("CMAKE_")) {
                    return;
                }

                suspiciousPoints.add(node);
            }
        });

        AbstractSimulator unusedSimulator = new AbstractSimulator() {
            @Override
            protected void process(SimulationState state, CommandInvocationNode node) {
                state.simulate(suspiciousPoints);
                super.process(state, node);
            }
        };

        unusedSimulator.simulate(new SimulationState(nodes, 0));

        suspiciousPoints.forEach(n -> reporter.report(new SourceRange(n.getStart(), n.getEnd()), "Value not used"));
    }

}
