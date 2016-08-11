package ru.urururu.cmakeedit.core;

/**
 * Created by okutane on 11/08/16.
 */
public class SourceRange {
    private final SourceRef start;
    private final SourceRef end;

    public SourceRange(SourceRef start, SourceRef end) {
        this.start = start;
        this.end = end;
    }

    public SourceRef getStart() {
        return start;
    }

    public SourceRef getEnd() {
        return end;
    }
}
