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
        if (reachedEnd()) {
            throw new IllegalStateException("End reached");
        }
        return contents.charAt(position);
    }

    @Override
    public SourceRef position() {
        return new SourceRef(position);
    }

    @Override
    public boolean reachedEnd() {
        return position == contents.length();
    }

    @Override
    public void advance() {
        if (reachedEnd()) {
            throw new IllegalStateException("End reached");
        }
        position++;
    }
}
