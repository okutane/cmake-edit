package ru.urururu.cmakeedit;

/**
 * Created by okutane on 06/07/16.
 */
public class UnexpectedCharacterException extends ParseException {
    public UnexpectedCharacterException(ParseContext ctx) {
        super("Unexpected character '" + ctx.peek() + "' at " + ctx.position().getOffset());
    }
}
