package ru.urururu.cmakeedit.core;

import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

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

                        skipWhitespace(ctx);
                    }
                } catch (ParseException e) {
                    if (logger.isInfoEnabled()) {
                        logger.info(e.getMessage() + " at " + ctx.position().getOffset() + " in context:" + ctx.getContext(20), e);
                    }

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
        } else if (Character.isAlphabetic(c) || c == '_' || c == '@') {
            result = parseCommandInvocation(ctx);
        } else if (c != '\n') {
            throw new UnexpectedCharacterException(ctx);
        }

        return result;
    }

    /**
     * command_invocation  ::=  space* identifier space* '(' arguments ')'
     * identifier          ::=  <match '[A-Za-z_][A-Za-z0-9_]*'>
     * arguments           ::=  argument? separated_arguments*
     * separated_arguments ::=  separation+ argument? | separation* '(' arguments ')'
     * separation          ::=  space | line_ending
     *
     * Precondition: Character.isAlphabetic(ctx.peek()) || ctx.peek() == '_'
     */
    static CommandInvocationNode parseCommandInvocation(ParseContext ctx) throws ParseException {
        char first = ctx.peek();

        if (first == '@') {
            return parseMacroInvocation(ctx);
        }

        SourceRef start = ctx.position();

        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(first);
        ctx.advance();
        while (!ctx.reachedEnd()) {
            char c = ctx.peek();
            if (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_') {
                nameBuilder.append(ctx.peek());
                ctx.advance();
            } else {
                break;
            }
        }

        skipSpaces(ctx);

        if (ctx.reachedEnd()) {
            throw new ParseException(ctx, "Unexpected end of source");
        }

        List<CommentNode> comments = new ArrayList<>();
        List<Node> arguments = ArgumentParser.parseArguments(ctx, comments);
        SourceRef end = ctx.position();
        ctx.advance();

        return new CommandInvocationNode(nameBuilder.toString(), arguments, comments, start, end);
    }

    private static CommandInvocationNode parseMacroInvocation(ParseContext ctx) throws ParseException {
        try (Timer.Context timing = ctx.getRegistry().timer("Parser.parseMacroInvocation").time()) {
            // super lazy mode
            SourceRef start = ctx.position();
            ctx.advance();

            StringBuilder nameBuilder = new StringBuilder();
            while (!ctx.reachedEnd() && ctx.peek() != '@') {
                nameBuilder.append(ctx.peek());
                ctx.advance();
            }

            if (ctx.reachedEnd()) {
                throw new ParseException(ctx, "Unexpected end of contents");
            }

            SourceRef end = ctx.position();
            ctx.advance();

            return new MacroInvocationNode(nameBuilder.toString(),
                    new ArrayList<>(), new ArrayList<>(),
                    start, end
            );
        }
    }

    /**
     * space        ::=  <match '[ \t]+'>
     */
    static void skipSpaces(ParseContext ctx) {
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
     * space        ::=  <match '[ \t]+'>
     * newline      ::=  <match '\n'>
     */
    private static void skipWhitespace(ParseContext ctx) throws UnexpectedCharacterException {
        while (!ctx.reachedEnd()) {
            switch (ctx.peek()) {
                case ' ':
                case '\t':
                case '\n':
                    ctx.advance();
                    continue;
                default:
                    return;
            }
        }
    }

    public enum ErrorHandling {
        Exception,
        NodesBefore,
        PartialResult
    }
}