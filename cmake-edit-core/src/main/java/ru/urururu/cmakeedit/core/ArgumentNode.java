package ru.urururu.cmakeedit.core;

import java.util.Collections;
import java.util.List;

/**
 * Created by okutane on 10/07/16.
 */
public class ArgumentNode extends Node {
    public static final ArgumentNode EMPTY =
            new ArgumentNode(Collections.emptyList(), new SourceRef(-1), new SourceRef(-1));

    private final List<Node> children;

    public ArgumentNode(List<Node> children) {
        this(children, children.get(0).getStart(), children.get(children.size() - 1).getEnd());
    }

    public ArgumentNode(ConstantNode constant) {
        this(Collections.singletonList(constant));
    }

    public ArgumentNode(List<Node> children, SourceRef start, SourceRef end) {
        super(start, end);
        this.children = children;
    }

    @Override
    public String toString() {
        return "children:" + children + " from:" + getStart().getOffset() + " to: " + getEnd().getOffset();
    }

    @Override
    public void visitAll(NodeVisitor visitor) {
        visitor.accept(this);
        if (children != null) {
            children.forEach(n -> n.visitAll(visitor));
        }
    }
}
