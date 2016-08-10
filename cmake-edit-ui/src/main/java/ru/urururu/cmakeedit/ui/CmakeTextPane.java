package ru.urururu.cmakeedit.ui;

import ru.urururu.cmakeedit.core.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;

/**
 * Created by okutane on 07/08/16.
 */
public class CmakeTextPane extends JScrollPane implements DocumentListener, NodeVisitor {
    private final JTextPane textPane;

    private final DefaultStyledDocument styledDocument;
    private final Style normal;
    private final Style comment;
    private final Style argument;

    public CmakeTextPane(String text) {
        styledDocument = new DefaultStyledDocument();

        Style parent = styledDocument.addStyle("parent", null);

        normal = styledDocument.addStyle("normal", parent);
        StyleConstants.setForeground(normal, Color.BLACK);

        comment = styledDocument.addStyle("comment", parent);
        StyleConstants.setForeground(comment, new Color(114, 114, 114));

        argument = styledDocument.addStyle("argument", parent);
        StyleConstants.setForeground(argument, new Color(13, 119, 0));

        textPane = new JTextPane(styledDocument);
        setViewportView(textPane);

        if (text != null) {
            textPane.replaceSelection(text);
            parseAll();
        }

        styledDocument.addDocumentListener(this);
    }

    private void parseAll() {
        FileNode fileNode;
        try {
            fileNode = Parser.parse(new DocumentParseContext(styledDocument), Parser.ErrorHandling.NodesBefore);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
        fileNode.visitAll(CmakeTextPane.this);
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
    public void accept(FileElementNode node) {
        colorize(node, normal);
    }

    @Override
    public void accept(ArgumentNode node) {
        colorize(node, argument);
    }

    @Override
    public void accept(CommentNode node) {
        colorize(node, comment);
    }

    private void colorize(Node node, Style style) {
        styledDocument.setCharacterAttributes(node.getStart().getOffset(), node.getEnd().getOffset() - node.getStart().getOffset() + 1, style, false);
    }
}
