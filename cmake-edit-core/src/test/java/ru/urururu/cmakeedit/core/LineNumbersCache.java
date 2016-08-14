package ru.urururu.cmakeedit.core;

import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by okutane on 14/08/16.
 */
class LineNumbersCache {
    private final int[] lineBreaks;

    LineNumbersCache(RandomAccessContext ctx) {
        String text = ctx.getText(0, ctx.getLength());

        List<Integer> lineBreaks = new ArrayList<>();
        int i = text.indexOf('\n');
        while (i != -1) {
            lineBreaks.add(i);
            i = text.indexOf('\n', i + 1);
        }

        this.lineBreaks = Ints.toArray(lineBreaks);
    }

    String getLineRange(SourceRange range) {
        int startLine = getLine(range.getStart());
        int endLine = getLine(range.getEnd());

        if (startLine == endLine) {
            return Integer.toString(startLine);
        }

        return Integer.toString(startLine) + '-' + endLine;
    }

    private int getLine(SourceRef ref) {
        return -Arrays.binarySearch(lineBreaks, ref.getOffset());
    }
}
