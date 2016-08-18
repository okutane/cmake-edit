package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.CommandInvocationNode;

/**
 * Created by okutane on 17/08/16.
 */
class UnexpectedCommandException extends LogicalException {
    UnexpectedCommandException(CommandInvocationNode command) {
        super("Unexpected " + command.getCommandName(), command, command);
    }
}
