package ru.urururu.cmakeedit.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Created by okutane on 23/07/16.
 */
public class Editor {
    public static void main(String... args) {
        String text = null;

        if (args.length == 1) {
            // First parameter is a path
            File source = new File(args[0]);

            if (source.exists() && source.isFile()) {
                try {
                    text = new String(Files.readAllBytes(source.toPath()), StandardCharsets.UTF_8);
                    text = text.replace("\r", "");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                throw new IllegalStateException("File not found. Should blame with messagebox about bad parameters");
            }
        }
        if (args.length == 2) {
            // alert about error
            throw new IllegalStateException("Not implemented yet. Should blame with messagebox about bad parameters");
        }

        CmakeTextPane textArea = new CmakeTextPane(text);

        JFrame frame = new JFrame("cmake-edit");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Add contents to the window.
        frame.add(textArea);

        //Display the window.
        frame.pack();
        frame.setMinimumSize(new Dimension(500, 500));
        frame.setVisible(true);
    }
}
