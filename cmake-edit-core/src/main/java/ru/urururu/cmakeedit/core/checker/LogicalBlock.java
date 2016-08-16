package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.CommandInvocationNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by okutane on 16/08/16.
 */
public class LogicalBlock {
    List<CommandInvocationNode> headers = new ArrayList<>();
    List<List<CommandInvocationNode>> bodies = new ArrayList<>();
    int endPosition;
}
