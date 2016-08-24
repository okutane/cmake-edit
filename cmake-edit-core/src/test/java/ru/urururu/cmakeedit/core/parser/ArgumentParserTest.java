package ru.urururu.cmakeedit.core.parser;

import org.junit.Assert;
import org.junit.Test;
import ru.urururu.cmakeedit.core.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by okutane on 16/07/16.
 */
public class ArgumentParserTest {
    @Test
    public void testPlain() throws ParseException {
        checkArguments("()");

        checkArguments("(a)", arg(val("a", 1, 1)));
        checkArguments("(\"a\")", arg(1, 3, val("a", 2, 2)));
        checkArguments("([=[a]=])", arg(val("", 1, 7)));
    }

    @Test
    public void testNested() throws ParseException {
        checkArguments("(FALSE AND (FALSE OR TRUE))",
                arg(val("FALSE", 1, 5)),
                arg(val("AND", 7, 9)),
                nested(12, 24,
                        arg(val("FALSE", 12, 16)),
                        arg(val("OR", 18, 19)),
                        arg(val("TRUE", 21, 24))
                )
        );
    }

    @Test
    public void testExpressions() throws ParseException {
        checkArguments("(${a})",
                arg(expr("", 1, 4, val("a", 3, 3)))
        );
        checkArguments("(${a})",
                arg(expr("", 1, 4, val("a", 3, 3))));
        checkArguments("($ENV{a})",
                arg(expr("ENV", 1, 7, val("a", 6, 6)))
        );
        checkArguments("(\"$a${b}\")", arg(1, 8, val("$a", 2, 3), expr("", 4, 7, val("b", 6, 6))));
    }

    @Test
    public void testSpecialCharacters() throws ParseException {
        checkArguments("(regex \"[0-9]+$\")",
                arg(val("regex", 1, 5)), arg(7, 15, val("[0-9]+$", 8, 14)));
    }

    @Test
    public void testLogicalExpression() throws ParseException {
        checkArguments("($<$<BOOL:$<CONFIGURATION>>:_$<CONFIGURATION>>)",
                arg(
                        expr("", 1, 45,
                                expr("", 3, 26,
                                        val("BOOL:", 5, 9),
                                        expr("", 10, 25, val("CONFIGURATION", 12, 24))
                                ),
                                val(":_", 26, 28),
                                expr("", 29, 44, val("CONFIGURATION", 31, 43)))
                )

        );
    }

    private ArgumentNode arg(int from, int to, Node... expressions) {
        return new ArgumentNode(Arrays.asList(expressions), new SourceRef(from), new SourceRef(to));
    }

    private ArgumentNode arg(Node... expressions) {
        return new ArgumentNode(Arrays.asList(expressions));
    }

    private ConstantNode val(String value, int from, int to) {
        return new ConstantNode(value, new SourceRef(from), new SourceRef(to));
    }

    private ExpressionNode expr(String key, int from, int to, Node... nested) {
        return new ExpressionNode(key, Arrays.asList(nested), new SourceRef(from), new SourceRef(to));
    }

    private ArgumentNode nested(int from, int to, ArgumentNode... children) {
        return new ArgumentNode(Arrays.asList(children), new SourceRef(from), new SourceRef(to));
    }

    private void checkArguments(String source, ArgumentNode... expected) throws ParseException {
        List<Node> expectedList = Arrays.asList(expected);
        List<Node> actualList = parseString(source);
        Assert.assertEquals("for source: " + source, expectedList.toString(), actualList.toString());
    }

    private List<Node> parseString(String source) throws ParseException {
        StringParseContext ctx = new StringParseContext(source, 0);
        return ArgumentParser.parseArguments(ctx, Collections.emptyList());
    }
}
