package ru.urururu.cmakeedit.core.checker;

import com.codahale.metrics.MetricRegistry;
import ru.urururu.cmakeedit.core.FileNode;

import java.util.List;

/**
 * Created by okutane on 18/08/16.
 */
class CheckContextDecorator implements CheckContext {
    private final CheckContext ctx;

    CheckContextDecorator(CheckContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public FileNode getAst() {
        return ctx.getAst();
    }

    @Override
    public MetricRegistry getRegistry() {
        return ctx.getRegistry();
    }

    @Override
    public ProblemReporter getReporter() {
        return ctx.getReporter();
    }

    @Override
    public List<SimulationState> getFunctionStates() {
        return ctx.getFunctionStates();
    }

    @Override
    public List<SimulationState> getLoopStates() {
        return ctx.getLoopStates();
    }
}
