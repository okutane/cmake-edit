package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.CommandInvocationNode;

import java.util.*;

/**
 * Created by okutane on 22/08/16.
 */
public class FunctionSimulator implements AbstractSimulator.CommandSimulator {
    private final AbstractSimulator simulator;
    private final List<CommandInvocationNode> body;
    private Stage stage = Stage.Unused;

    /** Variables from parent scopes are stored here. */
    private Set<String> unknownUsages = new HashSet<>();

    /** Variables written to parent scope are stored here. */
    private Map<String, Set<CommandInvocationNode>> parentVariables = new HashMap<>();

    public FunctionSimulator(AbstractSimulator simulator, List<CommandInvocationNode> body) {
        this.simulator = simulator;
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
                    protected void putValue(CommandInvocationNode command, String variable, boolean parentScope) {
                        if (parentScope) {
                            parentVariables.put(variable, Collections.singleton(command));
                        }

                        super.putValue(command, variable, parentScope);
                    }
                };

                simulator.simulate(functionCtx, entryState);

                stage = Stage.Used;
        }

        unknownUsages.forEach(state::processUsage);
        parentVariables.forEach((k, nodes) -> state.putValue(nodes.iterator().next(), k, false));

        state.setPosition(state.getPosition() + 1);
        return state;
    }

    private enum Stage {
        Unused,
        FirstUse,
        Used
    }
}
