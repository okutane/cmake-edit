package ru.urururu.cmakeedit.ui;

import com.codahale.metrics.MetricRegistry;
import ru.urururu.cmakeedit.core.*;
import ru.urururu.cmakeedit.core.checker.FileCheckContext;
import ru.urururu.cmakeedit.core.checker.Checker;
import ru.urururu.cmakeedit.core.checker.LogicalException;
import ru.urururu.cmakeedit.core.parser.Parser;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;

/**
 * Created by okutane on 07/08/16.
 */
class CmakeTextPane extends JScrollPane implements DocumentListener {
    private final JTextPane textPane;
    private boolean isDirty;
    private SwingWorker<Void, Void> worker = new CheckerWorker();

    private final DefaultStyledDocument styledDocument;
    private final Style normal;
    private final Style comment;
    private final Style argument;
    private final Style expression;

    private static final Highlighter.HighlightPainter warningsHighlighter =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(127, 127, 0, 50));
    private static final Highlighter.HighlightPainter errorsHighlighter =
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
            parseAll(true);
        }

        styledDocument.addDocumentListener(this);
    }

    private void parseAll(boolean force) {
        if (!(isDirty || force)) {
            return;
        }
        if (worker != null) {
            worker.cancel(true);
        }

        worker = new CheckerWorker();
        worker.execute();
        isDirty = false;
    }

    private void addHighlight(SourceRef start, SourceRef end, Highlighter.HighlightPainter painter) {
        try {
            textPane.getHighlighter().addHighlight(start.getOffset(), end.getOffset() + 1, painter);
        } catch (BadLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        onEvent(e.getType(), e.getOffset(), e.getLength(), e.getDocument());
    }

    private void onEvent(DocumentEvent.EventType type, int offset, int length, Document document) {
        isDirty = true;
        SwingUtilities.invokeLater(() -> parseAll(false));
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        onEvent(e.getType(), e.getOffset(), e.getLength(), e.getDocument());
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        // generated on style change, not interested.
    }

    private void colorize(Node node, Style style) {
        styledDocument.setCharacterAttributes(node.getStart().getOffset(), node.getEnd().getOffset() - node.getStart().getOffset() + 1, style, false);
    }

    private class CheckerWorker extends SwingWorker<Void, Void> {
        FileNode ast;
        Map<Highlighter.HighlightPainter, Collection<SourceRange>> pendingHighlights = new LinkedHashMap<>();


        @Override
        protected Void doInBackground() throws Exception {
            ast = Parser.parse(new DocumentParseContext(styledDocument), Parser.ErrorHandling.NodesBefore);

            Collection<SourceRange> warnings = new ArrayList<>();
            pendingHighlights.put(warningsHighlighter, warnings);

            try {
                Checker.findUnused(new FileCheckContext(ast, new MetricRegistry(), (range, problem) -> warnings.add(range)));
            } catch (LogicalException e) {
                pendingHighlights.put(errorsHighlighter,
                        Collections.singleton(new SourceRange(e.getFirstNode().getStart(), e.getLastNode().getEnd())));
            }

            return null;
        }

        @Override
        protected void done() {
            if (ast == null) {
                // partial results are ok, but ast is necessary.
                return;
            }

            styledDocument.setCharacterAttributes(0, styledDocument.getLength(), normal, true);
            textPane.getHighlighter().removeAllHighlights();

            ast.visitAll(new NodeVisitorAdapter() {
                @Override
                public void accept(CommandInvocationNode node) {
                    colorize(node, normal);
                    for (Node child : node.getComments()) {
                        child.visit(this);
                    }
                    for (Node child : node.getArguments()) {
                        child.visit(this);
                    }
                }

                @Override
                public void accept(ArgumentNode node) {
                    colorize(node, argument);
                    for (Node child : node.getChildren()) {
                        child.visit(this);
                    }
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
            });

            for (Map.Entry<Highlighter.HighlightPainter, Collection<SourceRange>> highlights : pendingHighlights.entrySet()) {
                Highlighter.HighlightPainter painter = highlights.getKey();
                highlights.getValue().forEach(problem -> addHighlight(problem.getStart(), problem.getEnd(), painter));
            }
        }
    }
}
