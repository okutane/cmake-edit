package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.CommandInvocationNode;

/**
 * Created by okutane on 16/08/16.
 */
public class LogicalException extends CheckerException {
    private final CommandInvocationNode firstNode;
    private final CommandInvocationNode lastNode;

    LogicalException(String message, CommandInvocationNode firstNode, CommandInvocationNode lastNode) {
        super(message);
        this.firstNode = firstNode;
        this.lastNode = lastNode;
    }

    public CommandInvocationNode getFirstNode() {
        return firstNode;
    }

    public CommandInvocationNode getLastNode() {
        return lastNode;
    }
}
