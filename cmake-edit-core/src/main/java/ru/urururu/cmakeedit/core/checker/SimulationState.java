package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.*;

import java.util.*;
import java.util.function.Function;

/**
 * Created by okutane on 16/08/16.
 */
class SimulationState {
    private final List<CommandInvocationNode> nodes;
    private int position;
    private final Map<String, Set<CommandInvocationNode>> variables;
    private final Map<String, AbstractSimulator.CommandSimulator> dynamicSimulators = new HashMap<>();

    SimulationState(List<CommandInvocationNode> nodes, int position) {
        this(nodes, position, new HashMap<>());
    }

    SimulationState(List<CommandInvocationNode> nodes, int position, Map<String, Set<CommandInvocationNode>> variables) {
        this.nodes = nodes;
        this.position = position;
        this.variables = variables;
    }

    void simulate(Set<Node> suspiciousPoints, Node node) {
        node.visitAll(new NodeVisitorAdapter() {
            @Override
            public void accept(CommandInvocationNode node) {
                processUsages(node);

                Function<CommandInvocationNode, Node> setter = Checker.setters.get(node.getCommandName());
                if (setter != null) {
                    Node argument = setter.apply(node);

                    if (argument != null) {
                        String variable = ((ArgumentNode) argument).getArgument();
                        // todo if variable is a reference through other variables we should inline it.
                        variables.put(variable, Collections.singleton(node));
                    }
                } else if (node.getCommandName().equals("unset")) {
                    List<Node> arguments = node.getArguments();
                    if (arguments.size() > 0) {
                        String variable = ((ArgumentNode) arguments.get(0)).getArgument();
                        // todo if variable is a reference through other variables we should inline it.

                        variables.remove(variable);
                    }
                }
            }

            private void processUsages(CommandInvocationNode node) {
                if (node.getCommandName().equals("set")) {
                    List<Node> arguments = node.getArguments();

                    if (arguments.isEmpty()) {
                        return;
                    }

                    arguments.get(0).visitAll(new NodeVisitorAdapter() {
                        @Override
                        public void accept(ExpressionNode node) {
                            processExpression(node, suspiciousPoints);
                        }
                    });

                    for (int i = 1; i < arguments.size(); i++) {
                        arguments.get(i).visitAll(new NodeVisitorAdapter() {
                            @Override
                            public void accept(ExpressionNode node) {
                                processExpression(node, suspiciousPoints);
                            }

                            @Override
                            public void accept(ArgumentNode node) {
                                // not to hardcore?
                                String argument = node.getArgument();
                                processUsage(argument, suspiciousPoints);
                            }
                        });
                    }

                    return;
                }

                node.visitAll(new NodeVisitorAdapter() {
                    @Override
                    public void accept(ExpressionNode node) {
                        processExpression(node, suspiciousPoints);
                    }

                    @Override
                    public void accept(ArgumentNode node) {
                        // not to hardcore?
                        String argument = node.getArgument();
                        processUsage(argument, suspiciousPoints);
                    }
                });
            }
        });
    }

    private void processExpression(ExpressionNode node, Set<Node> suspiciousPoints) {
        if (node.getKey() != null) {
            return;
        }

        // fixme dirty
        String expression = node.getExpression();
        int expressionStart = expression.indexOf('{');
        int expressionEnd = expression.lastIndexOf('}');

        if (expressionStart != -1 && expressionEnd != -1) {
            String argument = expression.substring(expressionStart + 1, expressionEnd);
            processUsage(argument, suspiciousPoints);
        }
    }

    private void processUsage(String argument, Set<Node> suspiciousPoints) {
        Set<CommandInvocationNode> commandInvocationNode = variables.get(argument);

        if (commandInvocationNode == null) {
            return;
        }

        suspiciousPoints.removeAll(commandInvocationNode);
    }

    public CommandInvocationNode getCurrent() {
        return nodes.get(position);
    }

    public int getPosition() {
        return position;
    }

    public List<CommandInvocationNode> getNodes() {
        return nodes;
    }

    public Map<String, Set<CommandInvocationNode>> getVariables() {
        return variables;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void addSimulator(String name, AbstractSimulator.CommandSimulator simulator) {
        dynamicSimulators.put(name, simulator);
    }

    public AbstractSimulator.CommandSimulator getSimulator(String name) {
        return dynamicSimulators.get(name);
    }
}
