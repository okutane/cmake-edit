package ru.urururu.cmakeedit.core;

/**
 * Created by okutane on 24/08/16.
 */
public class ConstantNode extends Node {
    private final String value;

    public ConstantNode(String value, SourceRef start, SourceRef end) {
        super(start, end);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void visit(NodeVisitor visitor) {
        visitor.accept(this);
    }

    @Override
    public String toString() {
        return value + ':' + start.getOffset() + '-' + end.getOffset();
    }
}
