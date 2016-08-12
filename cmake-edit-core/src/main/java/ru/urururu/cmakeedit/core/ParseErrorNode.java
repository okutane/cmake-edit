package ru.urururu.cmakeedit.core;

import java.util.Collections;

/**
 * Created by okutane on 07/07/16.
 */
public class ParseErrorNode extends FileElementNode {
    String message;

    public ParseErrorNode(ParseContext ctx, ParseException e, SourceRef start, SourceRef end) {
        super(Collections.emptyList(), start, end);
        this.message = e.getMessage();
    }

    @Override
    public void visitAll(NodeVisitor visitor) {
        visitor.accept(this);
    }
}
