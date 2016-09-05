package ru.urururu.cmakeedit.core;

import java.util.List;

/**
 * Created by okutane on 05/07/16.
 */
public class FileNode {
    private List<FileElementNode> nodes;

    public FileNode(List<FileElementNode> nodes) {
        this.nodes = nodes;
    }

    public List<FileElementNode> getNodes() {
        return nodes;
    }

    public void visitAll(NodeVisitor visitor) {
        for (FileElementNode node : nodes) {
            node.visit(visitor);
        }
    }
}
