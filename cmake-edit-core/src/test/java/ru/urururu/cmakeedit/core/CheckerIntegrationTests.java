package ru.urururu.cmakeedit.core;

import junit.framework.TestSuite;
import org.junit.Test;

import java.util.function.Function;

/**
 * Created by okutane on 11/08/16.
 */
public class CheckerIntegrationTests {
    @Test
    public static TestSuite suite() {
        return TestHelper.buildPack("/checkers/unused", ast -> Checker.findUnused(ast));
    }
}
