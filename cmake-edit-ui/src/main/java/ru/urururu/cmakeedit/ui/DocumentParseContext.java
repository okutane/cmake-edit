package ru.urururu.cmakeedit.ui;

import com.codahale.metrics.MetricRegistry;
import ru.urururu.cmakeedit.core.parser.RandomAccessContext;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Created by okutane on 07/08/16.
 */
class DocumentParseContext extends RandomAccessContext {
    private final Document document;

    DocumentParseContext(Document document) {
        super(new MetricRegistry(), 0);
        this.document = document;
    }

    @Override
    public String getText(int from, int to) {
        try {
            return document.getText(from, to - from);
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int getLength() {
        return document.getLength();
    }

    @Override
    public char peek() {
        try {
            return document.getText(position, 1).charAt(0);
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }
}
