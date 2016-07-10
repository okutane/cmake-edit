package ru.urururu.cmakeedit;

import com.codahale.metrics.Timer;

/**
 * Created by okutane on 09/07/16.
 */
public abstract class ArgumentParser {
    private String name;

    private ArgumentParser(String name) {
        this.name = name;
    }

    private Node parseExternal(ParseContext ctx) throws ParseException {
        try (Timer.Context timer = ctx.getRegistry().timer(name + ".parse").time()) {
            return parseInternal(ctx);
        }
    }

    Node parseInternal(ParseContext ctx) throws ParseException {
        throw new ParseException(ctx, name + " not supported");
    }

    /**
     * argument ::=  bracket_argument | quoted_argument | unquoted_argument
     */
    public static Node parse(ParseContext ctx) throws ParseException {
        char c = ctx.peek();
        if (c == '[') {
            return BRACKET.parseExternal(ctx);
        } else if (c == '"') {
            return QUOTED.parseExternal(ctx);
        } else {
            return UNQUOTED.parseExternal(ctx);
        }
    }

    private static final ArgumentParser BRACKET = new ArgumentParser("bracket_argument") {
        @Override
        Node parseInternal(ParseContext ctx) throws ParseException {
            return super.parseInternal(ctx);
        }
    };

    private static final ArgumentParser QUOTED = new ArgumentParser("quoted_argument") {
        @Override
        Node parseInternal(ParseContext ctx) throws ParseException {
            return super.parseInternal(ctx);
        }
    };

    /**
     * unquoted_argument ::=  unquoted_element+ | unquoted_legacy
     * unquoted_element  ::=  <any character except whitespace or one of '()#"\'> | escape_sequence
     * unquoted_legacy   ::=  <see note in text> todo
     */
    private static final ArgumentParser UNQUOTED = new ArgumentParser("unquoted_argument") {
        @Override
        Node parseInternal(ParseContext ctx) throws ParseException {
            SourceRef start = ctx.position();
            SourceRef end;
            StringBuilder sb = new StringBuilder();
            do {
                end = ctx.position();
                sb.append(ctx.peek());
                ctx.advance();

                if (ctx.peek() == '\\') {
                    throw new ParseException(ctx, "escape_sequence not supported");
                }
            } while (!ctx.reachedEnd() && isAllowed(ctx.peek()));
            return new ArgumentNode(sb.toString(), start, end);
        }

        private boolean isAllowed(char c) {
            if (c == ' ' || c == '\t') {
                return false;
            }
            if (c == '(' || c == ')' || c == '#' || c == '"') {
                return false;
            }
            return true;
        }
    };
}
