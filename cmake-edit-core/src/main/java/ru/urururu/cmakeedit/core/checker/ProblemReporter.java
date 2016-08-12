package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.SourceRange;

/**
 * Created by okutane on 11/08/16.
 */
public interface ProblemReporter {
    void report(SourceRange range, String problem);
}
