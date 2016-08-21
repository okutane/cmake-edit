package ru.urururu.cmakeedit.core.checker;

import junit.framework.TestSuite;
import org.junit.Test;
import ru.urururu.cmakeedit.core.TestHelper;

/**
 * Created by okutane on 11/08/16.
 */
public class CheckerTests {
    @Test
    public static TestSuite suite() {
        TestSuite suite = new TestSuite("Checker tests");
        suite.addTest(TestHelper.buildPack("/checkers/unused", Problem::findProblems, ".xml"));
        suite.addTest(TestHelper.buildPack("/checkers/errors", Problem::findErrors, ".xml"));
        return suite;
    }
}
