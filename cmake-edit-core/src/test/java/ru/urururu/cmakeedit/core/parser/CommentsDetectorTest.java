package ru.urururu.cmakeedit.core.parser;

import org.junit.Assert;
import org.junit.Test;
import ru.urururu.cmakeedit.core.parser.CommentsDetector;
import ru.urururu.cmakeedit.core.parser.ParseException;

/**
 * Created by okutane on 01/07/16.
 */
public class CommentsDetectorTest {
    @Test
    public void testLineComments() throws ParseException {
        checkComment(0, "#", 0);
        checkComment(6, "#simple", 0);
        checkComment(6, "#simple\nmessage(\"test\")", 0);

        checkComment(1, " #", 1);
        checkComment(7, " #simple", 1);
        checkComment(7, " #simple\nmessage(\"test\")", 1);
    }

    @Test
    public void testBlockComments() throws ParseException {
        checkComment(4, "#[[]]", 0);
        checkComment(11, "#[==[[\n]]==]", 0);
        checkComment(6, "#[=[]=]", 0);
    }

    private void checkComment(int expected, String content, int start) throws ParseException {
        Assert.assertEquals(content, expected, CommentsDetector.parseComment(content, start).getEnd().getOffset());
    }
}
