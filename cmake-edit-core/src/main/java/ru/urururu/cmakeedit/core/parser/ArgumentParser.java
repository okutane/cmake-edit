package ru.urururu.cmakeedit.core.parser;

import com.codahale.metrics.Timer;
import ru.urururu.cmakeedit.core.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by okutane on 09/07/16.
 */
abstract class ArgumentParser {
    private String name;

    private static List<ArgumentParser> parsers = Arrays.asList(
            new QuotedArgumentParser(),
            new BracketArgumentParser(),
            new UnquotedArgumentParser()
    );

    private ArgumentParser(String name) {
        this.name = name;
    }

    public final ArgumentNode parseExternal(ParseContext ctx) throws ParseException {
        try (Timer.Context timer = ctx.getRegistry().timer(name + ".parse").time()) {
            return parseInternal(ctx);
        } catch (ParseException e) {
            throw e; // $COVERAGE-IGNORE$
        } catch (Exception e) {
            throw new ParseException(ctx, e); // $COVERAGE-IGNORE$
        }
    }

    public final boolean canParse(ParseContext ctx) {
        try (Timer.Context timer = ctx.getRegistry().timer(name + ".canParse").time()) {
            return canParseInternal(ctx);
        }
    }

    abstract boolean canParseInternal(ParseContext ctx);

    abstract ArgumentNode parseInternal(ParseContext ctx) throws ParseException;

    /**
     * arguments           ::=  argument? separated_arguments*
     * separated_arguments ::=  separation+ argument? | separation* '(' arguments ')'
     * separation          ::=  space | line_ending
     */
    static List<Node> parseArguments(ParseContext ctx, List<CommentNode> comments) throws ParseException {
        char c = ctx.peek();
        if (c != '(') {
            throw new UnexpectedCharacterException(ctx);
        }
        ctx.advance();

        List<Node> arguments = new ArrayList<>();

        do {
            Parser.skipSpaces(ctx);

            if (ctx.reachedEnd()) {
                throw new ParseException(ctx, "Unexpected end of source");
            }
            c = ctx.peek();

            if (c == ')') {
                return arguments;
            } else if (c == '#') {
                comments.add(CommentsDetector.parseComment(ctx));
            } else if (c == '(') {
                List<Node> nested = parseArguments(ctx, comments);

                if (nested.isEmpty()) {
                    arguments.add(ArgumentNode.EMPTY);
                } else {
                    arguments.add(new ArgumentNode(nested));
                }
                ctx.advance();
            } else {
                ArgumentNode argument = ArgumentParser.parse(ctx);
                if (argument != null) {
                    arguments.add(argument);
                }
            }
        } while (true);
    }

    /**
     * argument ::=  bracket_argument | quoted_argument | unquoted_argument
     */
    public static ArgumentNode parse(ParseContext ctx) throws ParseException {
        for (ArgumentParser parser : parsers) {
            if (parser.canParse(ctx)) {
                return parser.parseExternal(ctx);
            }
        }
        throw new UnexpectedCharacterException(ctx);
    }

    /**
     * bracket_argument ::=  bracket_open bracket_content bracket_close
     * bracket_open     ::=  '[' '='{len} '['
     * bracket_content  ::=  <any text not containing a bracket_close of the same {len} as the bracket_open>
     * bracket_close    ::=  ']' '='{len} ']'
     */
    private static class BracketArgumentParser extends ArgumentParser {
        public BracketArgumentParser() {
            super("bracket_argument");
        }

        @Override
        boolean canParseInternal(ParseContext ctx) {
            SourceRef start = ctx.position();
            try {
                if (!ctx.reachedEnd() && ctx.peek() == '[') {
                    ctx.advance();
                    while (!ctx.reachedEnd() && ctx.peek() == '=') {
                        ctx.advance();
                    }
                    if (!ctx.reachedEnd() && ctx.peek() == '[') {
                        return true;
                    }
                }
                return false;
            } finally {
                ctx.move(start);
            }
        }

        @Override
        ArgumentNode parseInternal(ParseContext ctx) throws ParseException {
            SourceRef start = ctx.position();

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
            throw new UnexpectedCharacterException(ctx);
        }

        private ArgumentNode parseBracketComment(ParseContext ctx, SourceRef start, int len) throws ParseException {
            boolean firstBraceSeen = false;
            int closeLen = 0;
            while (!ctx.reachedEnd()) {
                if (ctx.peek() == ']') {
                    if (firstBraceSeen && closeLen == len) {
                        SourceRef end = ctx.position();
                        ctx.advance();
                        return new ArgumentNode(new ConstantNode("", start, end));
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

    /**
     * quoted_argument     ::=  '"' quoted_element* '"'
     * quoted_element      ::=  <any character except '\' or '"'> | escape_sequence | quoted_continuation
     * quoted_continuation ::=  '\' newline
     */
    private static class QuotedArgumentParser extends ArgumentParser {
        public QuotedArgumentParser() {
            super("quoted_argument");
        }

        @Override
        boolean canParseInternal(ParseContext ctx) {
            return ctx.peek() == '"';
        }

        @Override
        ArgumentNode parseInternal(ParseContext ctx) throws ParseException {
            List<Node> expressions = new ArrayList<>();

            SourceRef argumentStart = ctx.position();

            char prev = 0;

            ctx.advance();

            StringBuilder sb = new StringBuilder();
            SourceRef constantStart = ctx.position();
            SourceRef constantEnd = null;

            while (!ctx.reachedEnd()) {
                char cur = ctx.peek();
                if (prev == '\\') {
                    switch (cur) {
                        case '(':
                        case ')':
                        case '#':
                        case '"':
                        case ' ':
                        case '\\':
                        case '$':
                        case '@':
                        case '^':
                        case ';':
                            sb.append(cur);
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case '\n':
                            // line continuation
                            break;
                        default:
                            throw new UnexpectedCharacterException(ctx);
                    }
                    prev = 0;
                } else if (cur == '$' && ExpressionParser.canParse(ctx, true)) {
                    if (sb.length() != 0) {
                        expressions.add(new ConstantNode(sb.toString(), constantStart, constantEnd));
                    }

                    ExpressionNode expression = ExpressionParser.parseExpression(ctx);
                    expressions.add(expression);

                    // begin read of new constant
                    sb.setLength(0);
                    constantStart = null;
                    constantEnd = null;
                } else if (cur == '"') {
                    SourceRef argumentEnd = ctx.position();

                    ctx.advance();

                    if (sb.length() != 0) {
                        expressions.add(new ConstantNode(sb.toString(), constantStart, constantEnd));
                    }

                    return new ArgumentNode(expressions, argumentStart, argumentEnd);
                } else {
                    if (constantStart == null) {
                        constantStart = ctx.position();
                    }
                    constantEnd = ctx.position();
                    sb.append(cur);
                    prev = cur;
                }
                ctx.advance();
            }

            throw new ParseException(ctx, "Unexpected end of source");
        }
    }

    /**
     * unquoted_argument ::=  unquoted_element+ | unquoted_legacy
     * unquoted_element  ::=  <any character except whitespace or one of '()#"\'> | escape_sequence
     * unquoted_legacy   ::=  <see note in text> todo
     */
    private static class UnquotedArgumentParser extends ArgumentParser {
        public UnquotedArgumentParser() {
            super("unquoted_argument");
        }

        @Override
        boolean canParseInternal(ParseContext ctx) {
            return true;
        }

        @Override
        ArgumentNode parseInternal(ParseContext ctx) throws ParseException {
            List<Node> expressions = new ArrayList<>();

            SourceRef start = ctx.position();
            SourceRef end = start;
            StringBuilder sb = new StringBuilder();
            char prev = 0;

            while (!ctx.reachedEnd()) {
                char cur = ctx.peek();
                if (prev == '\\') {
                    // escape_sequence completion
                    switch (cur) {
                        case '(':
                        case ')':
                        case '#':
                        case '"':
                        case ' ':
                        case '\\':
                        case '$':
                        case '@':
                        case '^':
                        case ';':
                            sb.append(cur);
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case '\n':
                            // line continuation
                            break;
                        default:
                            throw new UnexpectedCharacterException(ctx);
                    }
                    prev = 0;
                } else if (cur == '\n')  {
                    if (sb.length() != 0) {
                        expressions.add(new ConstantNode(sb.toString(), start, end));
                    }

                    ctx.advance();

                    if (expressions.isEmpty()) {
                        return null;
                    }

                    return new ArgumentNode(expressions);
                } else if (cur == ' ' || cur == '\t' || cur == '"' || cur == '(' || cur == ')' || cur == '#')  {
                    if (sb.length() != 0) {
                        expressions.add(new ConstantNode(sb.toString(), start, end));
                    }

                    return new ArgumentNode(expressions);
                } else if (cur == '$') {// todo add canParse check?
                    if (sb.length() != 0) {
                        expressions.add(new ConstantNode(sb.toString(), start, end));
                    }

                    ExpressionNode expression = ExpressionParser.parseExpression(ctx);
                    expressions.add(expression);

                    // reset constant builder
                    sb.setLength(0);
                    start = null; // we're pointing at closing bracket now
                    end = null;
                } else {
                    if (start == null) {
                        start = ctx.position();
                    }
                    end = ctx.position();
                    sb.append(cur);
                    prev = cur;
                }
                ctx.advance();
            }

            throw new ParseException(ctx, "Unexpected end of source");
        }
    }
}
