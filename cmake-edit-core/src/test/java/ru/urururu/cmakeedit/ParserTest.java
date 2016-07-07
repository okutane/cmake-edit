package ru.urururu.cmakeedit;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by okutane on 07/07/16.
 */
public class ParserTest {
    @Test
    public void testEmptyLines() throws ParseException {
        FileNode emptyFile = parseString("");
        Assert.assertEquals(0, emptyFile.getNodes().size());

        FileNode emptyLinesFile = parseString("\n");
        Assert.assertEquals(0, emptyLinesFile.getNodes().size());
    }

    private FileNode parseString(String source) throws ParseException {
        return Parser.parse(new StringParseContext(source, 0));
    }
}
