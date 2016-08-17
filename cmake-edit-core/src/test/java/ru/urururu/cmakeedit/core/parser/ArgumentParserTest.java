package ru.urururu.cmakeedit.core.parser;

import org.junit.Assert;
import org.junit.Test;
import ru.urururu.cmakeedit.core.ArgumentNode;
import ru.urururu.cmakeedit.core.ExpressionNode;
import ru.urururu.cmakeedit.core.Node;
import ru.urururu.cmakeedit.core.SourceRef;
import ru.urururu.cmakeedit.core.parser.ArgumentParser;
import ru.urururu.cmakeedit.core.parser.ParseException;
import ru.urururu.cmakeedit.core.parser.StringParseContext;

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

        checkArguments("(a)", arg("a", 1, 1));
        checkArguments("(\"a\")", arg("a", 1, 3));
        checkArguments("([=[a]=])", arg("", 1, 7));
    }

    @Test
    public void testNested() throws ParseException {
        checkArguments("(FALSE AND (FALSE OR TRUE))",
                arg("FALSE", 1, 5),
                arg("AND", 7, 9),
                nested(12, 24,
                        arg("FALSE", 12, 16),
                        arg("OR", 18, 19),
                        arg("TRUE", 21, 24)
                )
        );
    }

    @Test
    public void testExpressions() throws ParseException {
        checkArguments("(${a})",
                arg("${a}", 1, 4, expr("", "${a}", 1, 4))
        );
        checkArguments("(${a})",
                arg("${a}", 1, 4, expr("", "${a}", 1, 4)));
        checkArguments("($ENV{a})",
                arg("$ENV{a}", 1, 7, expr("ENV", "$ENV{a}", 1, 7))
        );
        checkArguments("(\"$a${b}\")", arg("$a${b}", 1, 8, expr("", "${b}", 4, 7)));
    }

    @Test
    public void testSpecialCharacters() throws ParseException {
        checkArguments("(regex \"[0-9]+$\")",
                arg("regex", 1, 5), arg("[0-9]+$", 7, 15));
    }

    @Test
    public void testLogicalExpression() throws ParseException {
        checkArguments("($<$<BOOL:$<CONFIGURATION>>:_$<CONFIGURATION>>)",
                arg("$<$<BOOL:$<CONFIGURATION>>:_$<CONFIGURATION>>", 1, 45,
                        expr("", "$<$<BOOL:$<CONFIGURATION>>:_$<CONFIGURATION>>", 1, 45)
                )
        );
    }

    private ArgumentNode arg(String argument, int from, int to, Node... expressions) {
        return new ArgumentNode(argument, Arrays.asList(expressions), new SourceRef(from), new SourceRef(to));
    }

    private ExpressionNode expr(String key, String expr, int from, int to, Node... nested) {
        return new ExpressionNode(key, expr, Arrays.asList(nested), new SourceRef(from), new SourceRef(to));
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
