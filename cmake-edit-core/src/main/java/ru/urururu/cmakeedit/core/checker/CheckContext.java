package ru.urururu.cmakeedit.core.checker;

import com.codahale.metrics.MetricRegistry;
import ru.urururu.cmakeedit.core.FileNode;

import java.util.List;

/**
 * Created by okutane on 18/08/16.
 */
interface CheckContext {
    FileNode getAst();

    MetricRegistry getRegistry();

    ProblemReporter getReporter();

    /** todo think of better name, it's used for function/file/directory */
    List<SimulationState> getFunctionStates();

    List<SimulationState> getLoopStates();
}
