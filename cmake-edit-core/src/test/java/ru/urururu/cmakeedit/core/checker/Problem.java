package ru.urururu.cmakeedit.core.checker;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import ru.urururu.cmakeedit.core.FileNode;
import ru.urururu.cmakeedit.core.SourceRange;
import ru.urururu.cmakeedit.core.TestHelper;
import ru.urururu.cmakeedit.core.parser.RandomAccessContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by okutane on 14/08/16.
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
@XStreamAlias("problem")
public class Problem {
    private final String problem;
    private final String details;
    private final String lineRange;

    static {
        TestHelper.X_STREAM.alias("problem", Problem.class);

        TestHelper.X_STREAM.alias("exception", UnexpectedCommandException.class);
        TestHelper.X_STREAM.omitField(Throwable.class, "stackTrace");
        TestHelper.X_STREAM.omitField(Throwable.class, "suppressedExceptions");
    }

    private Problem(String problem, String details, String lineRange) {
        this.problem = problem;
        this.details = details;
        this.lineRange = lineRange;
    }

    public static List<Problem> findProblems(RandomAccessContext ctx, FileNode ast) {
        List<Problem> result = new ArrayList<>();

        LineNumbersCache lineNumbers = new LineNumbersCache(ctx);

        try {
            Checker.findUnused(new FileCheckContext(ast, TestHelper.REGISTRY, (range, problem) -> result.add(new Problem(problem, getText(ctx, range), lineNumbers.getLineRange(range)))));
        } catch (LogicalException e) {
            throw new IllegalStateException(e);
        }

        return result;
    }

    public static LogicalException findErrors(RandomAccessContext ctx, FileNode ast) {
        List<Problem> result = new ArrayList<>();

        LineNumbersCache lineNumbers = new LineNumbersCache(ctx);

        try {
            Checker.findUnused(new FileCheckContext(ast, TestHelper.REGISTRY, (range, problem) -> {}));
        } catch (LogicalException e) {
            return e;
        }

        throw new IllegalStateException("No errors found");
    }

    private static String getText(RandomAccessContext ctx, SourceRange range) {
        return ctx.getText(range.getStart().getOffset(), range.getEnd().getOffset() + 1);
    }

}
