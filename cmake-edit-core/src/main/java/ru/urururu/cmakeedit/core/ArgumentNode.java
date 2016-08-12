package ru.urururu.cmakeedit.core;

import java.util.Collections;
import java.util.List;

/**
 * Created by okutane on 10/07/16.
 */
public class ArgumentNode extends Node {
    public static final ArgumentNode EMPTY =
            new ArgumentNode(Collections.emptyList(), new SourceRef(-1), new SourceRef(-1));

    private final String argument;
    private final List<Node> children;

    public ArgumentNode(String argument, List<Node> expressions, SourceRef start, SourceRef end) {
        super(start, end);
        this.argument = argument;
        this.children = maskEmpty(expressions);
    }

    public ArgumentNode(List<Node> children, SourceRef start, SourceRef end) {
        super(start, end);
        this.argument = null;
        this.children = children;
    }

    @Override
    public String toString() {
        if (children != null) {
            return "children:" + children + " from:" + getStart().getOffset() + " to: " + getEnd().getOffset();
        }
        return "arg:" + argument + " from:" + getStart().getOffset() + " to: " + getEnd().getOffset();
    }

    @Override
    public void visitAll(NodeVisitor visitor) {
        visitor.accept(this);
        if (children != null) {
            children.forEach(n -> n.visitAll(visitor));
        }
    }

    public String getArgument() {
        return argument;
    }
}
