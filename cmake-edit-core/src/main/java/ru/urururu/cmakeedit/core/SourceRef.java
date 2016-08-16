package ru.urururu.cmakeedit.core;

import java.io.Serializable;

/**
 * Describes position in the source.
 *
 * Created by okutane on 07/07/16.
 */
public class SourceRef implements Serializable {
    private final int offset;

    public SourceRef(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }
}
