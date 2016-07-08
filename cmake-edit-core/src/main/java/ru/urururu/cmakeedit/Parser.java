package ru.urururu.cmakeedit;

import com.codahale.metrics.Timer;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    /**
     * file         ::=  file_element*
     * @param ctx
     * @return
     */
    public static FileNode parse(ParseContext ctx) throws ParseException {
        return parse(ctx, ErrorHandling.Exception);
    }

    /**
     * file         ::=  file_element*
     * @param ctx
     * @param errorHandling
     * @return
     */
    public static FileNode parse(ParseContext ctx, ErrorHandling errorHandling) throws ParseException {
        try (Timer.Context timing = ctx.getRegistry().timer("Parser.parse").time()) {
            List<FileElementNode> nodes = new ArrayList<>();

            if (!ctx.reachedEnd()) {
                SourceRef position = null;
                try {
                    while (!ctx.reachedEnd()) {
                        position = ctx.position();
                        FileElementNode node = parseFileNode(ctx);
                        if (!node.equals(FileElementNode.EMPTY)) {
                            nodes.add(node);
                        }
                    }
                } catch (ParseException e) {
                    if (errorHandling == ErrorHandling.Exception) {
                        throw e;
                    }
                    nodes.add(new ParseErrorNode(ctx, e, position));
                }
            }

            return new FileNode(nodes);
        }
    }

    /**
     * file_element ::=  command_invocation line_ending | (bracket_comment|space)* line_ending
     * @param ctx
     * @return
     */
    private static FileElementNode parseFileNode(ParseContext ctx) throws ParseException {
        skipSpaces(ctx);

        FileElementNode result = FileElementNode.EMPTY;

        char c = ctx.peek();
        if (c == '#') {
            List<CommentNode> comments = new ArrayList<>();

            do {
                comments.add(CommentsDetector.parseComment(ctx));
                skipSpaces(ctx);
            } while (!ctx.reachedEnd() && ctx.peek() != '\n');

            result = new FileElementNode(comments, comments.get(0).getStart(), comments.get(comments.size() - 1).getEnd());
        } else if (Character.isAlphabetic(c) || c == '_') {
            result = parseCommandInvocation(ctx);
        } else if (c != '\n') {
            throw new UnexpectedCharacterException(ctx);
        }

        skipNewline(ctx);
        return result;
    }

    /**
     * command_invocation  ::=  space* identifier space* '(' arguments ')'
     * identifier          ::=  <match '[A-Za-z_][A-Za-z0-9_]*'>
     * arguments           ::=  argument? separated_arguments*
     * separated_arguments ::=  separation+ argument? | separation* '(' arguments ')'
     * separation          ::=  space | line_ending
     * @param ctx
     * @return
     * @throws ParseException
     */
    private static FileElementNode parseCommandInvocation(ParseContext ctx) throws ParseException {
        throw new ParseException(ctx, "command_invocation not supported yet");
    }

    /**
     * space        ::=  <match '[ \t]+'>
     */
    private static void skipSpaces(ParseContext ctx) {
        while (!ctx.reachedEnd()) {
            switch (ctx.peek()) {
                case ' ':
                case '\t':
                    ctx.advance();
                    continue;
                default:
                    return;
            }
        }
    }

    /**
     * newline      ::=  <match '\n'>
     */
    private static void skipNewline(ParseContext ctx) throws UnexpectedCharacterException {
        if (ctx.reachedEnd()) {
            return;
        }

        char c = ctx.peek();
        if (c == '\n') {
            ctx.advance();
        } else {
            throw new UnexpectedCharacterException(ctx);
        }
    }

    public enum ErrorHandling {
        Exception,
        NodesBefore,
        PartialResult
    }
}