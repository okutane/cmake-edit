package ru.urururu.cmakeedit;

import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by okutane on 06/07/16.
 */
public class ParseException extends Exception {
    public ParseException(ParseContext ctx, String message) {
        super(message);

        ctx.getRegistry().counter(MetricRegistry.name(getClass(), message)).inc();
    }
}
