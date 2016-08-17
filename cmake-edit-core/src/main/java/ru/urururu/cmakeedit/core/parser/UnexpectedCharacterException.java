package ru.urururu.cmakeedit.core.parser;

/**
 * Created by okutane on 06/07/16.
 */
public class UnexpectedCharacterException extends ParseException {
    public UnexpectedCharacterException(ParseContext ctx) {
        super(ctx, "Unexpected character '" + ctx.peek() + '\'');
    }
}
