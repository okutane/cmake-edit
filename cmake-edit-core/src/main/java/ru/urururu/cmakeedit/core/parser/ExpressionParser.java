package ru.urururu.cmakeedit.core.parser;

import com.codahale.metrics.Timer;
import ru.urururu.cmakeedit.core.ExpressionNode;
import ru.urururu.cmakeedit.core.Node;
import ru.urururu.cmakeedit.core.SourceRef;

import java.util.*;

/**
 * Created by okutane on 11/08/16.
 */
public class ExpressionParser {
    private static final Map<Character, Character> BRACKETS = new HashMap<>();
    /** @see <a href='https://cmake.org/Wiki/CMake/Language_Syntax#CMake_may_access_environment_variables'>cmake language syntax</a>. */
    private static final List<String> ALLOWED_KEYS = Arrays.asList("", "ENV");

    static {
        BRACKETS.put('{', '}');
        BRACKETS.put('<', '>');
    }

    public static boolean canParse(ParseContext ctx, boolean insideQuoted) {
        // fixme should be well tested and rewritten
        try (Timer.Context timer = ctx.getRegistry().timer(ExpressionParser.class.getSimpleName() + ".canParse").time()) {
            SourceRef start = ctx.position();
            try {
                if (insideQuoted) {
                    char prev = '$';
                    ctx.advance();
                    while (!ctx.reachedEnd() && !BRACKETS.containsKey(ctx.peek()) && !(ctx.peek() == '"' && prev != '\\')) {
                        if (ctx.peek() == '$' && prev != '\\') {
                            return false;
                        }
                        if (ctx.peek() == ' ') {
                            return false;
                        }
                        prev = ctx.peek();
                        ctx.advance();
                    }
                } else {
                    while (!ctx.reachedEnd() && !BRACKETS.containsKey(ctx.peek())) {
                        ctx.advance();
                    }
                }
                return !ctx.reachedEnd() && BRACKETS.containsKey(ctx.peek());
            } finally {
                ctx.move(start);
            }
        }
    }

    public static ExpressionNode parseExpression(ParseContext ctx) throws ParseException {
        try (Timer.Context timer = ctx.getRegistry().timer(ExpressionParser.class.getSimpleName() + ".parseExpression").time()) {
            List<Node> nested = new ArrayList<>();
            SourceRef start = ctx.position();
            StringBuilder sb = new StringBuilder();

            sb.append(ctx.peek());
            ctx.advance();

            StringBuilder keyBuilder = new StringBuilder();
            while (!ctx.reachedEnd() && !BRACKETS.containsKey(ctx.peek())) {
                keyBuilder.append(ctx.peek());
                ctx.advance();
            }

            String key = keyBuilder.toString();
            if (!ALLOWED_KEYS.contains(key)) {
                throw new ParseException(ctx, "Key " + key + " is not used yet. For now only $ENV{..} is allowed");
            }

            sb.append(keyBuilder);

            if (ctx.reachedEnd()) {
                throw new ParseException(ctx, "Unexpected end of source");
            }

            char closingBracket = BRACKETS.get(ctx.peek());

            sb.append(ctx.peek());
            ctx.advance();

            while (!ctx.reachedEnd()) {
                char cur = ctx.peek();
                if (cur == closingBracket) {
                    sb.append(cur);
                    return new ExpressionNode(key, sb.toString(), nested, start, ctx.position());
                } else if (cur == '$') {
                    // we need to go deeper
                    ExpressionNode expression = ExpressionParser.parseExpression(ctx);
                    nested.add(expression);
                    sb.append(expression);
                } else {
                    sb.append(cur);
                }
                ctx.advance();
            }

            throw new ParseException(ctx, "Unexpected end of source");
        }
    }
}
