package ru.urururu.cmakeedit;

/**
 * Created by okutane on 10/07/16.
 */
public class ArgumentNode extends Node {
    private final String argument;

    public ArgumentNode(String argument, SourceRef start, SourceRef end) {
        super(start, end);
        this.argument = argument;
    }
}
