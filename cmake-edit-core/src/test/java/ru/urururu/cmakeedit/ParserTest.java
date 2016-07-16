package ru.urururu.cmakeedit;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.FieldDictionary;
import com.thoughtworks.xstream.converters.reflection.ImmutableFieldKeySorter;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by okutane on 07/07/16.
 */
public class ParserTest extends XMLTestCase {
    @Test
    public void testParseErrors() throws ParseException {
        try {
            parseString("[");
            fail("parse error ignored");
        } catch (ParseException e) {
            // expected
        }
    }

    @Test
    public void testEmptyLines() throws ParseException {
        FileNode emptyFile = parseString("");
        assertEquals(0, emptyFile.getNodes().size());

        FileNode emptyLinesFile = parseString("\n");
        assertEquals(0, emptyLinesFile.getNodes().size());
    }

    @Test
    public void testSeveralComments() throws ParseException {
        FileNode severalComments = parseString("#[]#[]");
    }

    @Test
    public void testParseCommandInvocation() throws Exception {
        XStream xstream = new XStream(new Sun14ReflectionProvider(
                new FieldDictionary(new ImmutableFieldKeySorter())),
                new DomDriver("utf-8"));

        CommandInvocationNode actual = Parser.parseCommandInvocation(new StringParseContext("test()", 0));
        CommandInvocationNode expected = new CommandInvocationNode("test",
                new ArrayList<>(),
                new ArrayList<>(),
                new SourceRef(0),
                new SourceRef(5)
        );

        assertXMLEqual(xstream.toXML(expected), xstream.toXML(actual));
    }

    private FileNode parseString(String source) throws ParseException {
        return Parser.parse(new StringParseContext(source, 0));
    }
}
