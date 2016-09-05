package ru.urururu.cmakeedit.core;

/**
 * Created by okutane on 24/08/16.
 */
public class ConstantNode extends Node {
    private final String value;

    public ConstantNode(String value, SourceRef start, SourceRef end) {
        super(start, end);

        // todo introduce validator for nodes and move code below there. keep in mind: escape sequences affect value length.
//        if (end.getOffset() + 1 - start.getOffset() != value.length()) {
//            throw new IllegalArgumentException("value = [" + value + "], start = [" + start + "], end = [" + end + "]");
//        }

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
