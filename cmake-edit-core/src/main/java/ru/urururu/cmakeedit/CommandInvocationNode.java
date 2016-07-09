package ru.urururu.cmakeedit;

import java.util.Collections;
import java.util.List;

/**
 * Created by okutane on 09/07/16.
 */
public class CommandInvocationNode extends FileElementNode {
    private final String commandName;

    public CommandInvocationNode(String commandName, List<Node> arguments, SourceRef start, SourceRef end) {
        super(Collections.emptyList(), start, end);
        this.commandName = commandName;
    }
}
