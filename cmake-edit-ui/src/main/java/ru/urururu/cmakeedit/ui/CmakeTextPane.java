package ru.urururu.cmakeedit.ui;

import ru.urururu.cmakeedit.core.*;
import ru.urururu.cmakeedit.core.checker.Checker;
import ru.urururu.cmakeedit.core.checker.LogicalException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;

/**
 * Created by okutane on 07/08/16.
 */
class CmakeTextPane extends JScrollPane implements DocumentListener, NodeVisitor {
    private final JTextPane textPane;

    private final DefaultStyledDocument styledDocument;
    private final Style normal;
    private final Style comment;
    private final Style argument;
    private final Style expression;

    private final Highlighter.HighlightPainter warningsHighlighter =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(127, 127, 0, 50));
    private final Highlighter.HighlightPainter errorsHighlighter =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 0, 0, 50));

    CmakeTextPane(String text) {
        styledDocument = new DefaultStyledDocument();

        Style parent = styledDocument.addStyle("parent", null);

        normal = styledDocument.addStyle("normal", parent);
        StyleConstants.setForeground(normal, Color.BLACK);

        comment = styledDocument.addStyle("comment", parent);
        StyleConstants.setForeground(comment, new Color(114, 114, 114));

        argument = styledDocument.addStyle("argument", parent);
        StyleConstants.setForeground(argument, new Color(13, 119, 0));

        expression = styledDocument.addStyle("expression", parent);
        StyleConstants.setForeground(expression, new Color(0, 119, 240));

        textPane = new JTextPane(styledDocument);
        setViewportView(textPane);

        if (text != null) {
            textPane.replaceSelection(text);
            parseAll();
        }

        styledDocument.addDocumentListener(this);
    }

    private void parseAll() {
        styledDocument.setCharacterAttributes(0, styledDocument.getLength(), normal, true);
        textPane.getHighlighter().removeAllHighlights();

        FileNode fileNode;
        try {
            fileNode = Parser.parse(new DocumentParseContext(styledDocument), Parser.ErrorHandling.NodesBefore);
        } catch (ParseException e) {
            // can't be thrown since we're using Parser.ErrorHandling.NodesBefore
            return;
        }

        fileNode.visitAll(CmakeTextPane.this);

        try {
            Checker.findUnused(fileNode, (range, problem) -> addHighlight(range.getStart(), range.getEnd(), warningsHighlighter));
        } catch (LogicalException e) {
            addHighlight(e.getFirstNode().getStart(), e.getLastNode().getEnd(), errorsHighlighter);
        }
    }

    private void addHighlight(SourceRef start, SourceRef end, Highlighter.HighlightPainter warningsHighlighter) {
        try {
            textPane.getHighlighter().addHighlight(start.getOffset(), end.getOffset() + 1, warningsHighlighter);
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        onEvent(e.getType(), e.getOffset(), e.getLength(), e.getDocument());
    }

    private void onEvent(DocumentEvent.EventType type, int offset, int length, Document document) {
        SwingUtilities.invokeLater(this::parseAll);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        onEvent(e.getType(), e.getOffset(), e.getLength(), e.getDocument());
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        // generated on style change, not interested.
    }

    @Override
    public void accept(CommandInvocationNode node) {
        colorize(node, normal);
    }

    @Override
    public void accept(ArgumentNode node) {
        colorize(node, argument);
    }

    @Override
    public void accept(ExpressionNode node) {
        colorize(node, expression);
    }

    @Override
    public void accept(CommentNode node) {
        colorize(node, comment);
    }

    @Override
    public void accept(ParseErrorNode node) {
        addHighlight(node.getStart(), node.getEnd(), errorsHighlighter);
    }

    private void colorize(Node node, Style style) {
        styledDocument.setCharacterAttributes(node.getStart().getOffset(), node.getEnd().getOffset() - node.getStart().getOffset() + 1, style, false);
    }
}
