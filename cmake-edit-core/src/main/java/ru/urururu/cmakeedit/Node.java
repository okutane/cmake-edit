package ru.urururu.cmakeedit;

/**
 * Created by okutane on 07/07/16.
 */
public class Node {
    protected final int start;
    protected final int end;

    public Node(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
