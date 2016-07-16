package ru.urururu.cmakeedit;

import com.codahale.metrics.Timer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by okutane on 09/07/16.
 */
abstract class ArgumentParser {
    private String name;

    private ArgumentParser(String name) {
        this.name = name;
    }

    private ArgumentNode parseExternal(ParseContext ctx) throws ParseException {
        try (Timer.Context timer = ctx.getRegistry().timer(name + ".parse").time()) {
            return parseInternal(ctx);
        }
    }

    ArgumentNode parseInternal(ParseContext ctx) throws ParseException {
        throw new ParseException(ctx, name + " not supported");
    }

    /**
     * arguments           ::=  argument? separated_arguments*
     * separated_arguments ::=  separation+ argument? | separation* '(' arguments ')'
     * separation          ::=  space | line_ending
     */
    static List<ArgumentNode> parseArguments(ParseContext ctx, List<CommentNode> comments) throws ParseException {
        char c = ctx.peek();
        if (c != '(') {
            throw new UnexpectedCharacterException(ctx);
        }
        ctx.advance();

        List<ArgumentNode> arguments = new ArrayList<>();

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
                List<ArgumentNode> nested = parseArguments(ctx, comments);

                if (nested.isEmpty()) {
                    arguments.add(ArgumentNode.EMPTY);
                } else {
                    arguments.add(new ArgumentNode(nested, nested.get(0).getStart(), nested.get(nested.size() - 1).getEnd()));
                }
                ctx.advance();
            } else {
                arguments.add(ArgumentParser.parse(ctx));
            }
        } while (true);
    }

    /**
     * argument ::=  bracket_argument | quoted_argument | unquoted_argument
     */
    public static ArgumentNode parse(ParseContext ctx) throws ParseException {
        char c = ctx.peek();
        if (c == '[') {
            return BRACKET.parseExternal(ctx);
        } else if (c == '"') {
            return QUOTED.parseExternal(ctx);
        } else {
            return UNQUOTED.parseExternal(ctx);
        }
    }

    /**
     * bracket_argument ::=  bracket_open bracket_content bracket_close
     * bracket_open     ::=  '[' '='{len} '['
     * bracket_content  ::=  <any text not containing a bracket_close of the same {len} as the bracket_open>
     * bracket_close    ::=  ']' '='{len} ']'
     */
    private static final ArgumentParser BRACKET = new ArgumentParser("bracket_argument") {
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
                        return new ArgumentNode("", start, end);
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
    };

    /**
     * quoted_argument     ::=  '"' quoted_element* '"'
     * quoted_element      ::=  <any character except '\' or '"'> | escape_sequence | quoted_continuation
     * quoted_continuation ::=  '\' newline
     */
    private static final ArgumentParser QUOTED = new ArgumentParser("quoted_argument") {
        @Override
        ArgumentNode parseInternal(ParseContext ctx) throws ParseException {
            SourceRef start = ctx.position();
            SourceRef end;
            StringBuilder sb = new StringBuilder();
            char prev = 0;

            ctx.advance();

            while (!ctx.reachedEnd()) {
                end = ctx.position();
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
                } else if (cur == '"')  {
                    ctx.advance();
                    return new ArgumentNode(sb.toString(), start, end);
                } else {
                    sb.append(cur);
                    prev = cur;
                }
                ctx.advance();
            }

            throw new ParseException(ctx, "Unexpected end of source");
        }
    };

    /**
     * unquoted_argument ::=  unquoted_element+ | unquoted_legacy
     * unquoted_element  ::=  <any character except whitespace or one of '()#"\'> | escape_sequence
     * unquoted_legacy   ::=  <see note in text> todo
     */
    private static final ArgumentParser UNQUOTED = new ArgumentParser("unquoted_argument") {
        @Override
        ArgumentNode parseInternal(ParseContext ctx) throws ParseException {
            SourceRef start = ctx.position();
            SourceRef end = start;
            StringBuilder sb = new StringBuilder();
            char prev = 0;

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
                } else if (cur == ' ' || cur == '\t' || cur == '"' || cur == '(' || cur == ')' || cur == '#')  {
                    return new ArgumentNode(sb.toString(), start, end);
                } else {
                    sb.append(cur);
                    prev = cur;
                }
                end = ctx.position();
                ctx.advance();
            }

            throw new ParseException(ctx, "Unexpected end of source");
        }
    };
}
