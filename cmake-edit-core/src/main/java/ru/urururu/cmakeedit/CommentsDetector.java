package ru.urururu.cmakeedit;

/**
 * Created by okutane on 01/07/16.
 */
public class CommentsDetector {
    static CommentNode parseComment(ParseContext ctx) throws ParseException {
        if (ctx.peek() != '#') {
            throw new UnexpectedCharacterException(ctx);
        }

        int start = ctx.position();

        if (ctx.hasMore()) {
            ctx.advance();
            int len = 0;
            while (ctx.peek() == '[') {
                len++;
                if (ctx.hasMore()) {
                    ctx.advance();
                } else {
                    break;
                }
            }
            if (len != 0) {
                return parseBracketComment(ctx, start, len);
            }
        }

        return parseLineComment(ctx, start);
    }

    static CommentNode parseComment(String contents, int start) throws ParseException {
        return parseComment(new StringParseContext(contents, start));
    }

    private static CommentNode parseLineComment(ParseContext ctx, int start) {
        while (ctx.hasMore() && ctx.peek() != '\n') {
            ctx.advance();
        }
        return new CommentNode(start, ctx.peek() == '\n' ? ctx.position() : ctx.position() + 1);
    }

    private static CommentNode parseBracketComment(ParseContext ctx, int start, int len) throws ParseException {
        int closeLen = 0;
        while (ctx.hasMore()) {
            if (ctx.peek() == ']') {
                closeLen++;
                if (closeLen == len) {
                    return new CommentNode(start, ctx.position() + 1);
                }
            } else {
                closeLen = 0;
            }
            ctx.advance();
        }
        if (ctx.peek() == ']') {
            closeLen++;
            if (closeLen == len) {
                return new CommentNode(start, ctx.position() + 1);
            }
        }
        throw new ParseException("Not expected end of content");
    }
}
