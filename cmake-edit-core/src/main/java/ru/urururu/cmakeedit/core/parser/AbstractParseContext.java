package ru.urururu.cmakeedit.core.parser;

import com.codahale.metrics.MetricRegistry;

/**
 * Created by okutane on 08/07/16.
 */
public abstract class AbstractParseContext implements ParseContext {
    private final MetricRegistry registry;

    protected AbstractParseContext(MetricRegistry registry) {
        this.registry = registry;
    }

    @Override
    public MetricRegistry getRegistry() {
        return registry;
    }
}
