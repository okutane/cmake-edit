package ru.urururu.cmakeedit.core;

import java.util.List;

/**
 * Created by okutane on 19/07/16.
 */
public class MacroInvocationNode extends CommandInvocationNode {
    public MacroInvocationNode(String commandName, List<Node> arguments, List<CommentNode> comments, SourceRef start, SourceRef end) {
        super(commandName, arguments, comments, start, end);
    }
}
