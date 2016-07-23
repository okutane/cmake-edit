package ru.urururu.cmakeedit.core;

/**
 * Created by okutane on 07/07/16.
 */
public class ParseErrorNode extends FileElementNode {
    String message;

    public ParseErrorNode(ParseContext ctx, ParseException e, SourceRef position) {
        super(null, position, position);
        this.message = e.getMessage();
    }
}
