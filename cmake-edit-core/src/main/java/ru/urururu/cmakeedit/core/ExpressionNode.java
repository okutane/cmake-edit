package ru.urururu.cmakeedit.core;

import java.util.List;

/**
 * Created by okutane on 11/08/16.
 */
public class ExpressionNode extends Node {
    private final String key;
    private String expression;
    private List<Node> nested;

    public ExpressionNode(String key, String expression, List<Node> nested, SourceRef start, SourceRef end) {
        super(start, end);
        this.key = maskEmpty(key);
        this.expression = expression;
        this.nested = maskEmpty(nested);
    }

    @Override
    public void visitAll(NodeVisitor visitor) {
        visitor.accept(this);
        if (nested != null) {
            nested.forEach(n -> n.visitAll(visitor));
        }
    }

    public String getKey() {
        return key;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return expression;
    }
}
