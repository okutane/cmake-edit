package ru.urururu.cmakeedit;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by okutane on 01/07/16.
 */
public class CommentsDetectorTest {
    @Test
    public void testLineComments() {
        checkComment(1, "#", 0);
        checkComment(7, "#simple", 0);
        checkComment(7, "#simple\nmessage(\"test\")", 0);

        checkComment(2, " #", 1);
        checkComment(8, " #simple", 1);
        checkComment(8, " #simple\nmessage(\"test\")", 1);
    }

    @Test
    public void testBlockComments() {
        checkComment(5, "#[[]]", 0);
        checkComment(8, "#[[[\n]]]", 0);
    }

    private void checkComment(int expected, String content, int start) {
        Assert.assertEquals(content, expected, CommentsDetector.parseComment(content, start).end());
    }
}
