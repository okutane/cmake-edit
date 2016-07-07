package ru.urururu.cmakeedit;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by okutane on 07/07/16.
 */
public class ParserTest {
    @Test
    public void testParseErrors() throws ParseException {
        try {
            parseString("[");
            fail("parse error ignored");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testEmptyLines() throws ParseException {
        FileNode emptyFile = parseString("");
        assertEquals(0, emptyFile.getNodes().size());

        FileNode emptyLinesFile = parseString("\n");
        assertEquals(0, emptyLinesFile.getNodes().size());
    }

    @Test
    public void severalComments() throws ParseException {
        FileNode severalComments = parseString("#[]#[]");
    }

    private FileNode parseString(String source) throws ParseException {
        return Parser.parse(new StringParseContext(source, 0));
    }
}
