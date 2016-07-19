package ru.urururu.cmakeedit;

import com.codahale.metrics.MetricRegistry;

/**
 * Created by okutane on 06/07/16.
 */
public class StringParseContext extends AbstractParseContext {
    private final String contents;
    private int position;

    public StringParseContext(String contents, int start) {
        this(contents, start, new MetricRegistry());
    }

    public StringParseContext(String contents, int start, MetricRegistry metricRegistry) {
        super(metricRegistry);
        this.contents = contents;
        this.position = start;
    }

    @Override
    public char peek() {
        if (reachedEnd()) {
            throw new IllegalStateException("End reached");
        }
        return contents.charAt(position);
    }

    @Override
    public SourceRef position() {
        return new SourceRef(position);
    }

    @Override
    public boolean reachedEnd() {
        return position == contents.length();
    }

    @Override
    public void advance() {
        if (reachedEnd()) {
            throw new IllegalStateException("End reached");
        }
        position++;
    }

    @Override
    public String getContext(int size) {
        int from = position - size / 2;
        int to = position + size / 2;

        from = Math.max(from, 0);
        to = Math.min(to, contents.length());

        return contents.substring(from, to);
    }
}
