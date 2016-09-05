package ru.urururu.cmakeedit.core;

import ru.urururu.cmakeedit.core.parser.ParseContext;
import ru.urururu.cmakeedit.core.parser.ParseException;

import java.util.Collections;

/**
 * Created by okutane on 07/07/16.
 */
public class ParseErrorNode extends FileElementNode {
    String message;

    public ParseErrorNode(ParseException e, SourceRef start, SourceRef end) {
        super(Collections.emptyList(), start, end);
        this.message = e.getMessage();
    }

    @Override
    public void visit(NodeVisitor visitor) {
        visitor.accept(this);
    }
}
