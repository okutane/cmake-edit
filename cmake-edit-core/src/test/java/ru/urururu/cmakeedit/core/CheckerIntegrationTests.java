package ru.urururu.cmakeedit.core;

import junit.framework.TestSuite;
import org.junit.Test;

/**
 * Created by okutane on 11/08/16.
 */
public class CheckerIntegrationTests {
    @Test
    public static TestSuite suite() {
        return TestHelper.buildPack("/integration", Problem::findProblems, ".unused.xml");
    }
}
