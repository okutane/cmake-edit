package ru.urururu.cmakeedit.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import ru.urururu.cmakeedit.core.checker.CheckContext;
import ru.urururu.cmakeedit.core.checker.Checker;
import ru.urururu.cmakeedit.core.checker.LogicalException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by okutane on 14/08/16.
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
@XStreamAlias("problem")
class Problem {
    private final String problem;
    private final String details;
    private final String lineRange;

    static {
        TestHelper.X_STREAM.alias("problem", Problem.class);
    }

    private Problem(String problem, String details, String lineRange) {
        this.problem = problem;
        this.details = details;
        this.lineRange = lineRange;
    }

    static List<Problem> findProblems(RandomAccessContext ctx, FileNode ast) {
        List<Problem> result = new ArrayList<>();

        LineNumbersCache lineNumbers = new LineNumbersCache(ctx);

        try {
            Checker.findUnused(new CheckContext(ast, TestHelper.REGISTRY, (range, problem) -> result.add(new Problem(problem, getText(ctx, range), lineNumbers.getLineRange(range)))));
        } catch (LogicalException e) {
            throw new IllegalStateException(e);
        }

        return result;
    }

    private static String getText(RandomAccessContext ctx, SourceRange range) {
        return ctx.getText(range.getStart().getOffset(), range.getEnd().getOffset() + 1);
    }

}
