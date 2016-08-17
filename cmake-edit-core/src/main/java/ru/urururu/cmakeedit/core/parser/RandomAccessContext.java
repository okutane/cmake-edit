package ru.urururu.cmakeedit.core.parser;

import com.codahale.metrics.MetricRegistry;
import ru.urururu.cmakeedit.core.SourceRef;

/**
 * Created by okutane on 07/08/16.
 */
public abstract class RandomAccessContext extends AbstractParseContext {
    protected int position;

    public RandomAccessContext(MetricRegistry registry, int start) {
        super(registry);
        this.position = start;
    }

    @Override
    public SourceRef position() {
        return new SourceRef(position);
    }

    @Override
    public void move(SourceRef position) {
        this.position = position.getOffset();
    }

    @Override
    public boolean reachedEnd() {
        return position == getLength();
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
        String prefix = "";
        String suffix = "";

        if (from < 0) {
            from = 0;
        } else {
            prefix = "...";
        }

        if (to > getLength()) {
            to = getLength();
        } else {
            suffix = "...";
        }

        return prefix + getText(from, to) + suffix;
    }

    public abstract String getText(int from, int to);

    public abstract int getLength();
}
