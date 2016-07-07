package ru.urururu.cmakeedit;

/**
 * Created by okutane on 01/07/16.
 */
public class CommentsDetector {
    static CommentNode parseComment(ParseContext ctx) throws ParseException {
        if (ctx.peek() != '#') {
            throw new UnexpectedCharacterException(ctx);
        }

        SourceRef start = ctx.position();

        ctx.advance();
        int len = 0;
        while (!ctx.reachedEnd() && ctx.peek() == '[') {
            len++;
            ctx.advance();
        }
        if (len != 0) {
            return parseBracketComment(ctx, start, len);
        }

        return parseLineComment(ctx, start);
    }

    static CommentNode parseComment(String contents, int start) throws ParseException {
        return parseComment(new StringParseContext(contents, start));
    }

    private static CommentNode parseLineComment(ParseContext ctx, SourceRef start) {
        SourceRef end = start;
        while (!ctx.reachedEnd() && ctx.peek() != '\n') {
            end = ctx.position();
            ctx.advance();
        }
        return new CommentNode(start, end);
    }

    private static CommentNode parseBracketComment(ParseContext ctx, SourceRef start, int len) throws ParseException {
        int closeLen = 0;
        while (!ctx.reachedEnd()) {
            if (ctx.peek() == ']') {
                closeLen++;
                if (closeLen == len) {
                    SourceRef end = ctx.position();
                    ctx.advance();
                    return new CommentNode(start, end);
                }
            } else {
                closeLen = 0;
            }
            ctx.advance();
        }
        throw new ParseException("Not expected end of content");
    }
}
