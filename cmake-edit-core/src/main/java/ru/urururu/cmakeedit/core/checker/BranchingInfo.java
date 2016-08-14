package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.CommandInvocationNode;
import ru.urururu.cmakeedit.core.FileElementNode;

import java.util.List;

/**
 * Created by okutane on 16/08/16.
 */
public class BranchingInfo {
    List<List<CommandInvocationNode>> branches;

    int mergePoint;
}
