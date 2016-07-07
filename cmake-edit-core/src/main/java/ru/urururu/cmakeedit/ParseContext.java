package ru.urururu.cmakeedit;

/**
 * Created by okutane on 06/07/16.
 */
public interface ParseContext {
    /**
     * @return character under current context cursor.
     */
    char peek();

    /**
     * @return reference position of current context cursor.
     */
    SourceRef position();

    /**
     * @return <code>true</code> if this ParseContext contains more text.
     */
    boolean hasMore();

    /**
     * moves current context cursor by 1 character.
     */
    void advance();
}
