package ru.urururu.cmakeedit.core;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by okutane on 11/08/16.
 */
public class ExpressionNode extends Node {
    private final String key;
    private List<Node> nested;

    public ExpressionNode(String key, List<Node> nested, SourceRef start, SourceRef end) {
        super(start, end);

        if (nested.isEmpty()) {
            throw new IllegalArgumentException("nested is empty");
        }

        this.key = maskEmpty(key);
        this.nested = nested;
    }

    @Override
    public void visit(NodeVisitor visitor) {
        visitor.accept(this);
    }

    public String getKey() {
        return key;
    }

    public List<Node> getNested() {
        return nested;
    }

    @Override
    public String toString() {
        return '$' + unmask(key) + '{' + nested.stream().map(Object::toString).collect(Collectors.joining()) + '}' + ':' + start.getOffset() + '-' + end.getOffset();
    }
}
