package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.ArgumentNode;
import ru.urururu.cmakeedit.core.CommandInvocationNode;
import ru.urururu.cmakeedit.core.Node;

import java.util.*;

/**
 * Created by okutane on 22/08/16.
 */
public class FunctionSimulator implements AbstractSimulator.CommandSimulator {
    private final AbstractSimulator simulator;
    private final List<String> formalParameters;
    private final List<CommandInvocationNode> body;
    private Stage stage = Stage.Unused;

    /** Variables from parent scopes are stored here. */
    private Set<String> unknownUsages = new HashSet<>();

    /** Variables written to parent scope are stored here. */
    private Map<ArgumentNode, Set<CommandInvocationNode>> parentVariables = new HashMap<>();

    FunctionSimulator(AbstractSimulator simulator, List<String> formalParameters, List<CommandInvocationNode> body) {
        this.simulator = simulator;
        this.formalParameters = formalParameters;
        this.body = body;
    }

    @Override
    public SimulationState simulate(CheckContext ctx, SimulationState state, CommandInvocationNode command) throws LogicalException {
        switch (stage) {
            case FirstUse:
                throw new LogicalException("recursion not supported", command, command);
            case Unused:
                stage = Stage.FirstUse;

                List<SimulationState> functionStates = new ArrayList<>();
                CheckContext functionCtx = new CheckContextDecorator(ctx) {
                    @Override
                    public List<SimulationState> getFunctionStates() {
                        return functionStates;
                    }

                    @Override
                    public List<SimulationState> getLoopStates() {
                        return null;
                    }
                };

                SimulationState entryState = new SimulationState(body, 0, state.getSuspiciousPoints()) {
                    @Override
                    protected void processUnknownUsage(String variable) {
                        unknownUsages.add(variable);
                    }

                    @Override
                    void putValue(ArgumentNode argumentNode, CommandInvocationNode command, boolean parentScope) {
                        if (parentScope) {
                            parentVariables.put(argumentNode, Collections.singleton(command));
                        }

                        super.putValue(argumentNode, command, parentScope);
                    }
                };

                simulator.simulate(functionCtx, entryState);

                stage = Stage.Used;
        }

        Map<String, Node> parameters = mapParameters(formalParameters, command.getArguments());

        unknownUsages.forEach(state::processUsage);
        parentVariables.forEach((k, nodes) -> state.putValue(k, nodes.iterator().next(), parameters, false));

        state.setPosition(state.getPosition() + 1);
        return state;
    }

    private static Map<String, Node> mapParameters(List<String> formalParameters, List<Node> actualParameters) {
        Map<String, Node> parameters = new LinkedHashMap<>();

        Iterator<String> keys = formalParameters.iterator();
        Iterator<Node> values = actualParameters.iterator();

        while (keys.hasNext() && values.hasNext()) {
            parameters.put(keys.next(), values.next());
        }
        return parameters;
    }

    private enum Stage {
        Unused,
        FirstUse,
        Used
    }
}
