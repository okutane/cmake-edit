package ru.urururu.cmakeedit.core.checker;

import com.codahale.metrics.MetricRegistry;
import ru.urururu.cmakeedit.core.FileNode;

public class CheckContext {
    private final FileNode ast;
    private MetricRegistry registry;
    private final ProblemReporter reporter;

    public CheckContext(FileNode ast, MetricRegistry registry, ProblemReporter reporter) {
        this.ast = ast;
        this.registry = registry;
        this.reporter = reporter;
    }

    public FileNode getAst() {
        return ast;
    }

    public MetricRegistry getRegistry() {
        return registry;
    }

    public ProblemReporter getReporter() {
        return reporter;
    }
}
