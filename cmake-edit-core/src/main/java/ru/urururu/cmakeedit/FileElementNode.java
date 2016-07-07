package ru.urururu.cmakeedit;

/**
 * Created by okutane on 05/07/16.
 */
public class FileElementNode extends Node {
    private CommentNode commentNode;

    public FileElementNode(CommentNode commentNode) {
        this(commentNode, commentNode.getStart(), commentNode.getEnd());
    }

    public FileElementNode(CommentNode commentNode, SourceRef start, SourceRef end) {
        super (start, end);
        this.commentNode = commentNode;
    }
}
