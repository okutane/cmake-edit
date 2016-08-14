package ru.urururu.cmakeedit.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by okutane on 14/08/16.
 */
public class LineNumbersCacheTest {
    @Test
    public void getLineRange() throws Exception {
        checkLines("a", 0, 0, "1");
        checkLines("a\nb", 0, 0, "1");
        checkLines("a\nb", 0, 2, "1-2");
    }

    private void checkLines(String source, int startOffset, int endOffset, String expected) {
        LineNumbersCache cache = new LineNumbersCache(new StringParseContext(source, 0));
        assertEquals(expected, cache.getLineRange(new SourceRange(new SourceRef(startOffset), new SourceRef(endOffset))));
    }
}