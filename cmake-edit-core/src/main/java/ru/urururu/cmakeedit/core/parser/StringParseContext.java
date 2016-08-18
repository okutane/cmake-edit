package ru.urururu.cmakeedit.core.parser;

import com.codahale.metrics.MetricRegistry;

/**
 * Created by okutane on 06/07/16.
 */
public class StringParseContext extends RandomAccessContext {
    private final String contents;

    public StringParseContext(String contents, int start) {
        this(contents, start, new MetricRegistry());
    }

    public StringParseContext(String contents, int start, MetricRegistry metricRegistry) {
        super(metricRegistry, start);
        this.contents = contents;
    }

    @Override
    public char peek() {
        if (reachedEnd()) {
            throw new IllegalStateException("End reached");
        }
        return contents.charAt(position);
    }

    @Override
    public String getText(int from, int to) {
        return contents.substring(from, to);
    }

    @Override
    public int getLength() {
        return contents.length();
    }
}
