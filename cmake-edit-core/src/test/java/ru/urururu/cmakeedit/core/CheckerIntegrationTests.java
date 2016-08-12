package ru.urururu.cmakeedit.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import junit.framework.TestSuite;
import org.junit.Test;
import ru.urururu.cmakeedit.core.checker.Checker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by okutane on 11/08/16.
 */
public class CheckerIntegrationTests {
    @Test
    public static TestSuite suite() {
        TestHelper.X_STREAM.alias("problem", Problem.class);

        return TestHelper.buildPack("/checkers/unused", CheckerIntegrationTests::findProblems);
    }

    private static List<Problem> findProblems(RandomAccessContext ctx, FileNode ast) {
        List<Problem> result = new ArrayList<>();

        Checker.findUnused(ast, (range, problem) -> result.add(new Problem(problem, getText(ctx, range), range)));

        return result;
    }

    private static String getText(RandomAccessContext ctx, SourceRange range) {
        return ctx.getText(range.getStart().getOffset(), range.getEnd().getOffset() + 1);
    }

    @SuppressWarnings("unused")
    @XStreamAlias("problem")
    public static class Problem {
        private final String problem;
        private final String details;
        private final SourceRange range;

        Problem(String problem, String details, SourceRange range) {
            this.problem = problem;
            this.details = details;
            this.range = range;
        }
    }
}
