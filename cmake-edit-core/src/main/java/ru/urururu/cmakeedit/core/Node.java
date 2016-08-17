package ru.urururu.cmakeedit.core;

import java.io.Serializable;
import java.util.List;

/**
 * Created by okutane on 07/07/16.
 */
public abstract class Node implements Serializable {
    protected final SourceRef start;
    protected final SourceRef end;

    protected Node(SourceRef start, SourceRef end) {
        if ((start != null && end != null) && start.getOffset() > end.getOffset()) {
            throw new IllegalArgumentException(start.getOffset() + " > " + end.getOffset());
        }
        if (start != end) {
            if (start == null) {
                throw new IllegalArgumentException("start == null");
            }
            if (end == null) {
                throw new IllegalArgumentException("end == null");
            }
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

    List<Node> maskEmpty(List<Node> list) {
        return list.isEmpty() ? null : list;
    }

    String maskEmpty(String string) {
        return string.isEmpty() ? null : string;
    }

    public abstract void visitAll(NodeVisitor visitor);
}
