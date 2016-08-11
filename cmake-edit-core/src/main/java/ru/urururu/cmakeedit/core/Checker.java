package ru.urururu.cmakeedit.core;

import java.util.*;

/**
 * Created by okutane on 11/08/16.
 */
public class Checker {
    public static Map<SourceRange, String> findUnused(FileNode ast) {
        // super naive checker

        Map<SourceRange, String> result = new LinkedHashMap<>();

        Map<String, SourceRange> stores = new HashMap<>();

        ast.visitAll(new NodeVisitor() {
            @Override
            public void accept(ArgumentNode node) {

            }

            @Override
            public void accept(CommentNode node) {

            }

            @Override
            public void accept(CommandInvocationNode node) {
                if (node.getCommandName().equals("set")) {
                    List<Node> arguments = node.getArguments();

                    if (!arguments.isEmpty()) {
                        ArgumentNode first = (ArgumentNode) arguments.get(0);
                        SourceRange unused = stores.put(first.getArgument(), new SourceRange(node.getStart(), node.getEnd()));

                        if (unused != null) {
                            result.put(unused, "Value replaced");
                        }
                    }
                }
            }

            @Override
            public void accept(ExpressionNode node) {

            }
        });

        stores.forEach((var, range) -> result.put(range, "Value not used"));

        return result;
    }
}
