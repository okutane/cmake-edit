package ru.urururu.cmakeedit;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    private ArgumentNode arg(String argument, int from, int to) {
        return new ArgumentNode(argument, new SourceRef(from), new SourceRef(to));
    }

    private ArgumentNode nested(int from, int to, ArgumentNode... children) {
        return new ArgumentNode(Arrays.asList(children), new SourceRef(from), new SourceRef(to));
    }

    private void checkArguments(String source, ArgumentNode... expected) throws ParseException {
        List<ArgumentNode> expectedList = Arrays.asList(expected);
        List<ArgumentNode> actualList = parseString(source);
        Assert.assertEquals("for source: " + source, expectedList.toString(), actualList.toString());
    }

    private List<ArgumentNode> parseString(String source) throws ParseException {
        StringParseContext ctx = new StringParseContext(source, 0);
        return ArgumentParser.parseArguments(ctx, Collections.emptyList());
    }
}
