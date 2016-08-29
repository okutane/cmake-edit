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

    String getNodeFromState(Node argumentNode) {
        StringBuilder sb = new StringBuilder();

        argumentNode.visitAll(new NodeVisitorAdapter() {
            @Override
            public void accept(ExpressionNode node) {
                StringBuilder exprBuilder = new StringBuilder();

                if (node.getKey() != null) {
                    exprBuilder.append(node.getKey());
                }

                node.getNested().stream().forEach(n -> exprBuilder.append(getNodeFromState(n)));

                String expr = exprBuilder.toString();
                getValue(expr);

                sb.append('*').append(expr);
            }

            @Override
            public void accept(ConstantNode node) {
                String value = node.getValue();
                if (value.endsWith("\n")) {
                    value = value.replace("\n", ""); // todo this hack should be replaced by fix in parser!
                }
                sb.append(value);
            }
        });

        return sb.toString();
    }

    void getValue(ArgumentNode argumentNode) {
        getNodeFromState(argumentNode);

        getValue(getArgument(argumentNode));
    }

    protected Set<CommandInvocationNode> getDefault(String variable) {
        return Collections.emptySet();
    }

    void getValue(String variable) {
        Set<CommandInvocationNode> nodes = variables.get(variable);

        if (nodes == null) {
            nodes = getDefault(variable);
        }

        suspiciousPoints.removeAll(nodes);
    }

    void putValue(ArgumentNode argumentNode, CommandInvocationNode command) {
        putValue(argumentNode, command, false);
    }

    void putValue(ArgumentNode argumentNode, CommandInvocationNode command, boolean parentScope) {
        // expressions from argumentNode should be used

        // todo inline argumentNode via getValue()
        variables.put(getNodeFromState(argumentNode), Collections.singleton(command));
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

    void simulate(Node node) {
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
                List<Node> arguments = node.getArguments();

                for (Node argument : arguments) {
                    String arg = getNodeFromState(argument);
                    getValue(arg); // todo shouldnt do for all!
                }
            }
        });
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
