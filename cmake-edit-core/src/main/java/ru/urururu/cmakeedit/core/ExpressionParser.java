package ru.urururu.cmakeedit.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by okutane on 11/08/16.
 */
public class ExpressionParser {
    public static ExpressionNode parseExpression(ParseContext ctx) throws ParseException {
        List<Node> nested = new ArrayList<>();
        SourceRef start = ctx.position();
        StringBuilder sb = new StringBuilder();

        sb.append(ctx.peek());
        ctx.advance();

        if (ctx.peek() != '{') {
            throw new UnexpectedCharacterException(ctx);
        }

        sb.append(ctx.peek());
        ctx.advance();

        while (!ctx.reachedEnd()) {
            char cur = ctx.peek();
            if (cur == '}')  {
                sb.append(cur);
                return new ExpressionNode(sb.toString(), nested, start, ctx.position());
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
