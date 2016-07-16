package ru.urururu.cmakeedit;

import java.util.Collections;
import java.util.List;

/**
 * Created by okutane on 09/07/16.
 */
public class CommandInvocationNode extends FileElementNode {
    private final String commandName;
    private List<ArgumentNode> arguments;

    public CommandInvocationNode(String commandName, List<ArgumentNode> arguments, List<CommentNode> comments, SourceRef start, SourceRef end) {
        super(comments, start, end);
        this.commandName = commandName;
        this.arguments = arguments;
    }
}
