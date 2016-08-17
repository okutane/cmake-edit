package ru.urururu.cmakeedit.core.parser;

import ru.urururu.cmakeedit.core.CommentNode;
import ru.urururu.cmakeedit.core.SourceRef;

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
        if (!ctx.reachedEnd() && ctx.peek() == '[') {
            ctx.advance();
            int len = 0;
            while (!ctx.reachedEnd() && ctx.peek() == '=') {
                len++;
                ctx.advance();
            }
            if (!ctx.reachedEnd() && ctx.peek() == '[') {
                ctx.advance();
                return parseBracketComment(ctx, start, len);
            }
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
        boolean firstBraceSeen = false;
        int closeLen = 0;
        while (!ctx.reachedEnd()) {
            if (ctx.peek() == ']') {
                if (firstBraceSeen && closeLen == len) {
                    SourceRef end = ctx.position();
                    ctx.advance();
                    return new CommentNode(start, end);
                }
                firstBraceSeen = true;
                closeLen = 0;
            } else if (ctx.peek() == '=') {
                closeLen++;
            } else {
                firstBraceSeen = false;
            }

            ctx.advance();
        }
        throw new ParseException(ctx, "Not expected end of content");
    }
}
