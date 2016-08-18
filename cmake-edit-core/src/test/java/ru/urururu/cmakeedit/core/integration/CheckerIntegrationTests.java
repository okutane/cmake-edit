package ru.urururu.cmakeedit.core.integration;

import junit.framework.TestSuite;
import org.junit.Test;
import ru.urururu.cmakeedit.core.TestHelper;
import ru.urururu.cmakeedit.core.checker.Problem;

/**
 * Created by okutane on 11/08/16.
 */
public class CheckerIntegrationTests {
    @Test
    public static TestSuite suite() {
        return TestHelper.buildPack("/integration", Problem::findProblems, ".unused.xml");
    }
}
