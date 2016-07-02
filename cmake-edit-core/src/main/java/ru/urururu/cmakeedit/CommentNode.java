package ru.urururu.cmakeedit;

/**
 * Created by okutane on 01/07/16.
 */
public class CommentNode {
    private final int start;
    private final int end;

    public CommentNode(int start, int end) {

        this.start = start;
        this.end = end;
    }

    public int end() {
        return end;
    }
}
