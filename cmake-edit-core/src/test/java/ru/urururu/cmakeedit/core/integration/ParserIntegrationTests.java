package ru.urururu.cmakeedit.core.integration;

import junit.framework.TestSuite;
import org.junit.Test;
import ru.urururu.cmakeedit.core.TestHelper;

/**
 * Created by okutane on 07/07/16.
 */
public class ParserIntegrationTests {
    @Test
    public static TestSuite suite() {
        return TestHelper.buildPack("/integration", (ctx, ast) -> ast, ".xml");
    }
}
