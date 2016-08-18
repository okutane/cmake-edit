package ru.urururu.cmakeedit.core.checker;

import com.codahale.metrics.MetricRegistry;
import ru.urururu.cmakeedit.core.FileNode;

import java.util.ArrayList;
import java.util.List;

public class FileCheckContext implements CheckContext {
    private final FileNode ast;
    private MetricRegistry registry;
    private final ProblemReporter reporter;
    private final List<SimulationState> fileStates = new ArrayList<>();

    public FileCheckContext(FileNode ast, MetricRegistry registry, ProblemReporter reporter) {
        this.ast = ast;
        this.registry = registry;
        this.reporter = reporter;
    }

    @Override
    public FileNode getAst() {
        return ast;
    }

    @Override
    public MetricRegistry getRegistry() {
        return registry;
    }

    @Override
    public ProblemReporter getReporter() {
        return reporter;
    }

    @Override
    public List<SimulationState> getFunctionStates() {
        return fileStates;
    }

    @Override
    public List<SimulationState> getLoopStates() {
        return null;
    }
}
