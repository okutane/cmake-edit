package ru.urururu.cmakeedit.core;

/**
 * Created by okutane on 25/07/16.
 */
public interface NodeVisitor {
    void accept(ArgumentNode node);

    void accept(CommentNode node);

    void accept(FileElementNode node);
}
