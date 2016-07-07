package ru.urururu.cmakeedit;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    /**
     * file         ::=  file_element*
     * @param ctx
     * @return
     */
    public static FileNode parse(ParseContext ctx) throws ParseException {
        List<FileElementNode> nodes = new ArrayList<>();

        SourceRef position = null;
        try {
            while (ctx.hasMore()) {
                position = ctx.position();
                nodes.add(parseFileNode(ctx));
            }
        } catch (ParseException e) {
            nodes.add(new ParseErrorNode(ctx, e, position));
        }

        return new FileNode(nodes);
    }

    /**
     * file_element ::=  command_invocation line_ending | (bracket_comment|space)* line_ending
     * @param ctx
     * @return
     */
    private static FileElementNode parseFileNode(ParseContext ctx) throws ParseException {
        skipSpaces(ctx);

        FileElementNode result;

        char c = ctx.peek();
        if (c == '#') {
            result = new FileElementNode(CommentsDetector.parseComment(ctx));
        } else if (Character.isAlphabetic(c) || c == '_') {
            result = parseCommandInvocation(ctx);
        } else {
            throw new UnexpectedCharacterException(ctx);
        }

        skipNewline(ctx);
        return result;
    }

    private static FileElementNode parseCommandInvocation(ParseContext ctx) throws ParseException {
        throw new ParseException("Not supported yet");
    }

    /**
     * space        ::=  <match '[ \t]+'>
     */
    private static void skipSpaces(ParseContext ctx) {
        char c = ctx.peek();
        while (c == ' ' || c == '\t') {
            ctx.advance();
            c = ctx.peek();
        }
    }

    /**
     * newline      ::=  <match '\n'>
     */
    private static void skipNewline(ParseContext ctx) throws UnexpectedCharacterException {
        char c = ctx.peek();
        if (c == '\n') {
            ctx.advance();
        } else {
            throw new UnexpectedCharacterException(ctx);
        }
    }

}