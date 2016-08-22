package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.CommandInvocationNode;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by okutane on 22/08/16.
 */
class MacroSimulator implements AbstractSimulator.CommandSimulator {
    private AbstractSimulator simulator;
    private List<CommandInvocationNode> body;

    MacroSimulator(AbstractSimulator simulator, List<CommandInvocationNode> body) {
        this.simulator = simulator;
        this.body = body;
    }

    @Override
    public SimulationState simulate(CheckContext ctx, SimulationState state, CommandInvocationNode command) throws LogicalException {
        SimulationState newState = simulator.simulate(ctx, new SimulationState(body, 0, new LinkedHashMap<>(state.getVariables())));
        if (newState != null) {
            return simulator.merge(Collections.singletonList(newState), state.getNodes(), state.getPosition() + 1);
        }

        return null;
    }
}
