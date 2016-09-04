package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.CommandInvocationNode;
import ru.urururu.cmakeedit.core.Node;

import java.util.Collections;
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
        for (Node node : command.getArguments()) {
            state.getValue(node);
        }

        SimulationState newState = simulator.simulate(ctx, state.copyAt(body, 0));
        if (newState != null) {
            return state.merge(state.getNodes(), state.getPosition() + 1, Collections.singletonList(newState));
        }

        return null;
    }
}
