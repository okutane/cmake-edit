package ru.urururu.cmakeedit;

/**
 * Created by okutane on 07/07/16.
 */
public class ParseErrorNode extends FileElementNode {
    String message;

    public ParseErrorNode(ParseContext ctx, ParseException e, int position) {
        super(null, position, ctx.position() + 1);
        this.message = e.getMessage();
    }
}
