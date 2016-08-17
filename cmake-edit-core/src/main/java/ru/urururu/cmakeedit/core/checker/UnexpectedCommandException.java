package ru.urururu.cmakeedit.core.checker;

import ru.urururu.cmakeedit.core.CommandInvocationNode;

/**
 * Created by okutane on 17/08/16.
 */
public class UnexpectedCommandException extends LogicalException {
    public UnexpectedCommandException(CommandInvocationNode command) {
        super("Unexpected " + command.getCommandName(), command, command);

        System.out.println("command.getCommandName() = " + command.getCommandName());
        System.exit(1); // todo remove!
    }
}
