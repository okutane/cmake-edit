package ru.urururu.cmakeedit.core;

import java.util.List;

/**
 * Created by okutane on 09/07/16.
 */
public class CommandInvocationNode extends FileElementNode {
    private final String commandName;
    private List<Node> arguments;

    public CommandInvocationNode(String commandName, List<Node> arguments, List<CommentNode> comments, SourceRef start, SourceRef end) {
        super(comments, start, end);
        this.commandName = commandName.toLowerCase();
        this.arguments = arguments;
    }

    @Override
    public void visitAll(NodeVisitor visitor) {
        visitor.accept(this);
        super.visitAll(visitor);
        arguments.forEach(n -> n.visitAll(visitor));
    }

    public String getCommandName() {
        return commandName;
    }

    public List<Node> getArguments() {
        return arguments;
    }
}
