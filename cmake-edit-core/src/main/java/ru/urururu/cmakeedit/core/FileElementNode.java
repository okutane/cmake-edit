package ru.urururu.cmakeedit.core;

import java.util.Collections;
import java.util.List;

/**
 * Created by okutane on 05/07/16.
 */
public class FileElementNode extends Node {
    public static final FileElementNode EMPTY = new FileElementNode(Collections.emptyList(), null, null);

    protected List<CommentNode> comments;

    public FileElementNode(List<CommentNode> comments, SourceRef start, SourceRef end) {
        super (start, end);
        this.comments = comments;
    }

    @Override
    public void visitAll(NodeVisitor visitor) {
        for (CommentNode node : comments) {
            node.visitAll(visitor);
        }
    }
}