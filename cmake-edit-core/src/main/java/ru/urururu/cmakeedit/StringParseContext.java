package ru.urururu.cmakeedit;

/**
 * Created by okutane on 06/07/16.
 */
public class StringParseContext implements ParseContext {
    private final String contents;
    private int position;

    public StringParseContext(String contents, int start) {
        this.contents = contents;
        this.position = start;
    }

    @Override
    public char peek() {
        return contents.charAt(position);
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public boolean hasMore() {
        return position + 1 < contents.length();
    }

    @Override
    public void advance() {
        position++;
    }
}
