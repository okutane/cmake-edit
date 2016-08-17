package ru.urururu.cmakeedit.core.parser;

import com.codahale.metrics.MetricRegistry;

/**
 * Created by okutane on 06/07/16.
 */
public class ParseException extends Exception {
    public ParseException(ParseContext ctx, String message) {
        super(message);

        ctx.getRegistry().counter(MetricRegistry.name(getClass(), message)).inc();
    }
}
