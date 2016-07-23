package ru.urururu.cmakeedit.core;

import java.util.Collections;
import java.util.List;

/**
 * Created by okutane on 05/07/16.
 */
public class FileElementNode extends Node {
    public static final FileElementNode EMPTY = new FileElementNode(Collections.emptyList(), null, null);

    private List<CommentNode> comments;

    public FileElementNode(List<CommentNode> comments, SourceRef start, SourceRef end) {
        super (start, end);
        this.comments = comments;
    }
}
