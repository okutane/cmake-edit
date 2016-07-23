package ru.urururu.cmakeedit.core;

/**
 * Created by okutane on 07/07/16.
 */
public abstract class Node {
    protected final SourceRef start;
    protected final SourceRef end;

    protected Node(SourceRef start, SourceRef end) {
        if ((start != null || end != null) && start.getOffset() > end.getOffset()) {
            throw new IllegalArgumentException(start.getOffset() + " > " + end.getOffset());
        }
        this.start = start;
        this.end = end;
    }

    /**
     * @return Reference to first character of the node.
     */
    public SourceRef getStart() {
        return start;
    }

    /**
     * @return Reference to last character of the node.
     */
    public SourceRef getEnd() {
        return end;
    }
}
