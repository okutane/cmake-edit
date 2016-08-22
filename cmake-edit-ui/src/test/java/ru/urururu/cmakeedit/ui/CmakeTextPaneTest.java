package ru.urururu.cmakeedit.ui;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by okutane on 22/08/16.
 */
public class CmakeTextPaneTest {
    @Test
    public void serialization() {
        CmakeTextPane textPane = new CmakeTextPane("");

        CmakeTextPane clone = SerializationUtils.clone(textPane);
    }
}