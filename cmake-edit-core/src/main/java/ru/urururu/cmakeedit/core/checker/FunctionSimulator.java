package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.CommandInvocationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by okutane on 22/08/16.
 */
public class FunctionSimulator implements AbstractSimulator.CommandSimulator {
    private final AbstractSimulator simulator;
    private final List<CommandInvocationNode> body;
    private Stage stage = Stage.Unused;
    private SimulationState exitState;

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

                SimulationState entryState = new SimulationState(body, 0, state.getSuspiciousPoints());

                exitState = simulator.simulate(functionCtx, entryState);
                stage = Stage.Used;
        }

        state.setPosition(state.getPosition() + 1);
        return state;
    }

    private enum Stage {
        Unused,
        FirstUse,
        Used
    }
}
