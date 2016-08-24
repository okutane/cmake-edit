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
    private final Set<CommandInvocationNode> suspiciousPoints;
    private final Map<String, AbstractSimulator.CommandSimulator> dynamicSimulators = new HashMap<>();

    SimulationState(List<CommandInvocationNode> nodes, int position, Set<CommandInvocationNode> suspiciousPoints) {
        this(nodes, position, suspiciousPoints, new HashMap<>());
    }

    SimulationState(List<CommandInvocationNode> nodes, int position, Set<CommandInvocationNode> suspiciousPoints, Map<String, Set<CommandInvocationNode>> variables) {
        this.nodes = nodes;
        this.position = position;
        this.variables = variables;
        this.suspiciousPoints = suspiciousPoints;
    }

    String getValue(ArgumentNode argumentNode) {
        return getValue(getArgument(argumentNode));
    }

    String getValue(String expression) {
        int expressionStart = expression.indexOf("${"); // todo add support for ENV
        int expressionEnd = expression.lastIndexOf('}');

        String result;
        if (expressionStart != -1 && expressionEnd > expressionStart) {
            String sub = expression.substring(expressionStart + 2, expressionEnd);

            // todo remove suspicious point for sub or for getValue(sub)?

            result = expression.substring(0, expressionStart) + getValue(sub) + expression.substring(expressionEnd);
        } else {
            result = expression;
        }

        suspiciousPoints.removeAll(variables.getOrDefault(result, Collections.emptySet()));

        return result;
    }

    void putValue(ArgumentNode argumentNode, CommandInvocationNode command) {
        putValue(argumentNode, command, false);
    }

    void putValue(ArgumentNode argumentNode, CommandInvocationNode command, boolean parentScope) {
        // todo inline argumentNode via getValue()
        variables.put(getArgument(argumentNode), Collections.singleton(command));
    }

    static String getArgument(Node argumentNode) {
        StringBuilder sb = new StringBuilder();

        argumentNode.visitAll(new NodeVisitorAdapter() {
            @Override
            public void accept(ExpressionNode node) {
                if (node.getKey() != null) {
                    sb.append(node.getKey());
                }
                //throw new IllegalStateException("not implemented");
                node.getNested().stream().forEach(n -> n.visitAll(this));
            }

            @Override
            public void accept(ConstantNode node) {
                sb.append(node.getValue());
            }
        });

        return sb.toString();
    }

    void simulate(Set<CommandInvocationNode> suspiciousPoints, Node node) {
        node.visitAll(new NodeVisitorAdapter() {
            @Override
            public void accept(CommandInvocationNode node) {
                processUsages(node);

                Function<CommandInvocationNode, Node> setter = Checker.setters.get(node.getCommandName());
                if (setter != null) {
                    Node argument = setter.apply(node);

                    if (argument != null) {
                        String variable = getArgument(argument);
                        // todo if variable is a reference through other variables we should inline it.
                        variables.put(variable, Collections.singleton(node));
                    }
                } else if (node.getCommandName().equals("unset")) {
                    List<Node> arguments = node.getArguments();
                    if (arguments.size() > 0) {
                        String variable = getArgument(arguments.get(0));
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
                            public void accept(ConstantNode node) {
                                processUsage(node.getValue(), suspiciousPoints);
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
                        processUsage(getArgument(node), suspiciousPoints);
                    }
                });
            }
        });
    }

    private void processExpression(ExpressionNode node, Set<CommandInvocationNode> suspiciousPoints) {
        if (node.getKey() != null) {
            return;
        }

        NodeVisitor visitor = new NodeVisitorAdapter() {
            @Override
            public void accept(ConstantNode node) {
                processUsage(node.getValue(), suspiciousPoints);
            }
        };

        node.visitAll(visitor);
    }

    private void processUsage(String argument, Set<CommandInvocationNode> suspiciousPoints) {
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

    public Set<CommandInvocationNode> getSuspiciousPoints() {
        return suspiciousPoints;
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
