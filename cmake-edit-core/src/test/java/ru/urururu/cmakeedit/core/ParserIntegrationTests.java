package ru.urururu.cmakeedit.core;

import junit.framework.TestSuite;
import org.junit.Test;

import java.util.function.Function;

/**
 * Created by okutane on 07/07/16.
 */
public class ParserIntegrationTests {
    @Test
    public static TestSuite suite() {
        return TestHelper.buildPack("/integration", (ctx, ast) -> ast);
    }
}
