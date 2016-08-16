package ru.urururu.cmakeedit.core.checker;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import ru.urururu.cmakeedit.core.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by okutane on 11/08/16.
 */
public class Checker {
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
                    if (!node.getCommandName().equals("set")) {
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

            unusedSimulator.simulate(ctx, new SimulationState(nodes, 0));

            suspiciousPoints.forEach(n -> ctx.getReporter().report(new SourceRange(n.getStart(), n.getEnd()), "Value not used"));
        }
    }
}
