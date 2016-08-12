package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.*;

import java.util.*;
import java.util.concurrent.BlockingDeque;

/**
 * Created by okutane on 11/08/16.
 */
public class Checker {
    public static void findUnused(FileNode ast, ProblemReporter reporter) {
        List<FileElementNode> nodes = ast.getNodes();

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

        Queue<SimulationState> states = new LinkedList<>();

        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i) instanceof CommandInvocationNode) {
                states.offer(new SimulationState(nodes, i));
                break;
            }
        }

        while (!states.isEmpty() && !suspiciousPoints.isEmpty()) {
            SimulationState state = states.poll();
            state.simulate(states, suspiciousPoints);
        }

        suspiciousPoints.forEach(n -> reporter.report(new SourceRange(n.getStart(), n.getEnd()), "Value not used"));
    }

    private static class SimulationState {
        static final List<String> builtins = Arrays.asList("include_directories");

        private final List<FileElementNode> nodes;
        private final int position;
        private final Map<String, CommandInvocationNode> variables;
        private final Stack<Integer> jumps;

        public SimulationState(List<FileElementNode> nodes, int position) {
            this(nodes, position, new HashMap<>(), new Stack<>());
        }

        public SimulationState(List<FileElementNode> nodes, int position, Map<String, CommandInvocationNode> variables, Stack<Integer> jumps) {
            this.nodes = nodes;
            this.position = position;
            this.variables = variables;
            this.jumps = jumps;
        }

        public void simulate(Queue<SimulationState> states, Set<Node> suspiciousPoints) {
            Node node = nodes.get(position);

            node.visitAll(new NodeVisitorAdapter() {
                @Override
                public void accept(CommandInvocationNode node) {
                    processUsages(node);

                    if (node.getCommandName().equalsIgnoreCase("if")) {
                        List<Integer> elseifPositions = new ArrayList<>();
                        Integer elsePosition = null;
                        int endifPosition;

                        int depth = 0;

                        for (int i = position + 1; i < nodes.size(); i++) {
                            if (nodes.get(i) instanceof CommandInvocationNode) {
                                CommandInvocationNode futureCommand = (CommandInvocationNode) nodes.get(i);
                                switch (futureCommand.getCommandName().toLowerCase()) {
                                    case "if":
                                        depth++;
                                        continue;
                                    case "elseif":
                                        if (depth == 0) {
                                            processUsages(futureCommand);
                                            elseifPositions.add(i);
                                        }
                                        continue;
                                    case "else":
                                        if (depth == 0) {
                                            elsePosition = i;
                                        }
                                        continue;
                                    case "endif":
                                        if (depth == 0) {
                                            // do the needful
                                            endifPosition = i;
                                            List<Integer> blockPositions = new ArrayList<>();
                                            blockPositions.add(position); // if
                                            blockPositions.addAll(elseifPositions); // elseifs
                                            blockPositions.add(elsePosition != null ? elsePosition : endifPosition); // else explicit or empty implicit
                                            blockPositions.add(endifPosition); // endif

                                            processIf(blockPositions);
                                            return;
                                        }
                                        depth--;
                                }
                            }
                        }
                    } else {
                        if (node.getCommandName().equalsIgnoreCase("set")) {
                            List<Node> arguments = node.getArguments();
                            if (arguments.size() > 0) {
                                String variable = ((ArgumentNode) arguments.get(0)).getArgument();
                                // todo if variable is a reference through other variables we should inline it.

                                variables.put(variable, node);
                            }
                        } else if (node.getCommandName().equalsIgnoreCase("unset")) {
                            List<Node> arguments = node.getArguments();
                            if (arguments.size() > 0) {
                                String variable = ((ArgumentNode) arguments.get(0)).getArgument();
                                // todo if variable is a reference through other variables we should inline it.

                                variables.remove(variable);
                            }
                        } /*else if (builtins.contains(node.getCommandName().toLowerCase())) {
                            // verified ok.
                        } else {
                            throw new IllegalStateException(node.getCommandName());
                        }*/
                    }

                    for (int newPosition = getNext(position); newPosition < nodes.size(); newPosition = getNext(newPosition)) {
                        if (nodes.get(newPosition) instanceof CommandInvocationNode) {
                            states.offer(new SimulationState(nodes, newPosition, variables, jumps));
                            return;
                        }
                    }
                }

                private void processUsages(CommandInvocationNode node) {
                    node.visitAll(new NodeVisitorAdapter() {
                        @Override
                        public void accept(ExpressionNode node) {
                            // fixme dirty
                            String expression = node.getExpression();
                            int expressionStart = expression.indexOf('{');
                            int expressionEnd = expression.lastIndexOf('}');

                            if (expressionStart != -1 && expressionEnd != -1) {
                                String argument = expression.substring(expressionStart + 1, expressionEnd);
                                CommandInvocationNode commandInvocationNode = variables.get(argument);
                                suspiciousPoints.remove(commandInvocationNode);
                            }
                        }
                    });
                }

                private int getNext(int current) {
                    if (jumps.isEmpty()) {
                        return current + 1;
                    } else if (current + 1 < jumps.peek()) {
                        return current + 1;
                    }
                    jumps.pop();
                    return jumps.pop();
                }

                private void processIf(List<Integer> blockPositions) {
                    // todo evaluate expressions from if and elseifs.

                    for (int i = 0; i < blockPositions.size() - 1; i++) {
                        for (int newPosition = blockPositions.get(i) + 1; newPosition < blockPositions.get(i + 1); newPosition++) {
                            if (nodes.get(newPosition) instanceof CommandInvocationNode) {
                                Stack<Integer> newJumps = (Stack<Integer>) jumps.clone();
                                int jumpTo = blockPositions.get(blockPositions.size() - 1) + 1;
                                newJumps.push(jumpTo); // jump to past endif.
                                int jumpFrom = blockPositions.get(i + 1);
                                newJumps.push(jumpFrom); // jump from elseif/else/endif.
                                states.offer(new SimulationState(nodes, newPosition, new HashMap<>(variables), newJumps));
                                break;
                            }
                        }
                    }
                }
            });
        }
    }
}
