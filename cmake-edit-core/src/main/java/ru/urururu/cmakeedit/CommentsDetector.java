package ru.urururu.cmakeedit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by okutane on 01/07/16.
 */
public class CommentsDetector {
    public static List<CommentNode> findAll(String contents) {
        List<CommentNode> comments = new ArrayList<>();

        int pos = 0;
        while (pos < contents.length()) {
            char c = contents.charAt(pos);
            if (c == '#') {
                CommentNode comment = parseComment(contents, pos);
                pos = comment.end();
            }
        }

        return comments;
    }

    static CommentNode parseComment(String contents, int start) {
        if (start + 1 < contents.length() && contents.charAt(start + 1) == '[') {
            int len = 1;
            while (contents.charAt(start + len + 1) == '[') {
                len++;
            }
            return parseBracketComment(contents, start, len);
        }

        return parseLineComment(contents, start);
    }

    private static CommentNode parseLineComment(String contents, int start) {
        int end = start + 1;
        while (end < contents.length() && contents.charAt(end) != '\n') {
            end++;
        }
        return new CommentNode(start, end);
    }

    private static CommentNode parseBracketComment(String contents, int start, int len) {
        String close = new String(new char[len]).replace("\0", "]");
        int end = contents.indexOf(close, start);
        if (end == -1) {
            return null; // todo throw ParseException("not closed bracked comment")
        }
        return new CommentNode(start, end + len);
    }
}
