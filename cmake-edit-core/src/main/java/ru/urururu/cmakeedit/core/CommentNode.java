package ru.urururu.cmakeedit.core;

/**
 * Created by okutane on 01/07/16.
 */
public class CommentNode extends Node {
    public CommentNode(SourceRef start, SourceRef end) {
        super(start, end);
    }

    @Override
    public void visitAll(NodeVisitor visitor) {
        visitor.accept(this);
    }
}
