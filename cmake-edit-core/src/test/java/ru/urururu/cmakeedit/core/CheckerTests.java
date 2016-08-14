package ru.urururu.cmakeedit.core;

import junit.framework.TestSuite;
import org.junit.Test;

/**
 * Created by okutane on 11/08/16.
 */
public class CheckerTests {
    @Test
    public static TestSuite suite() {
        return TestHelper.buildPack("/checkers/unused", Problem::findProblems, ".xml");
    }
}
