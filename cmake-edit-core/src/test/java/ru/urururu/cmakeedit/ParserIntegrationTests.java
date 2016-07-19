package ru.urururu.cmakeedit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.FieldDictionary;
import com.thoughtworks.xstream.converters.reflection.ImmutableFieldKeySorter;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;
import com.thoughtworks.xstream.io.xml.DomDriver;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.concurrent.TimeUnit;

/**
 * Created by okutane on 07/07/16.
 */
public class ParserIntegrationTests {
    static File ROOT;
    static String SRC_ROOT = System.getProperty("SRC_ROOT");
    static boolean UPDATE = Boolean.getBoolean("AUTO_UPDATE_ON_DIFF");

    @Test
    public static TestSuite suite() {
        URL url = ParserIntegrationTests.class.getResource("/integration");
        ROOT = new File(url.getFile());

        MetricRegistry registry = new MetricRegistry();

        TestSuite pack = createSuite(ROOT, registry);

        pack.addTest(new TestCase("Report results") {
            @Override
            protected void runTest() throws Throwable {
                ConsoleReporter reporter = ConsoleReporter.forRegistry(registry)
                        .convertRatesTo(TimeUnit.SECONDS)
                        .convertDurationsTo(TimeUnit.MILLISECONDS)
                        .build();

                reporter.report();
            }
        });

        return pack;
    }

    private static TestSuite createSuite(File file, MetricRegistry registry) {
        TestSuite suite = new TestSuite(file.getName());

        for (File child : file.listFiles()) {
            if (child.getName().endsWith(".xml")) {
                continue;
            }

            if (child.isDirectory()) {
                suite.addTest(createSuite(child, registry));
            } else {
                suite.addTest(new XMLTestCase(child.getName()) {
                    @Override
                    protected void runTest() throws Throwable {
                        String text = new String(Files.readAllBytes(child.toPath()), StandardCharsets.UTF_8);

                        text = text.replace("\r", "");

                        FileNode result = Parser.parse(new StringParseContext(text, 0, registry));

                        XStream xstream = new XStream(new Sun14ReflectionProvider(
                                new FieldDictionary(new ImmutableFieldKeySorter())),
                                new DomDriver("utf-8"));

                        String actual = xstream.toXML(result);

                        File expectedFile = new File(child.getAbsolutePath() + ".xml");
                        String expected;
                        try {
                            expected = new String(Files.readAllBytes(expectedFile.toPath()), StandardCharsets.UTF_8);
                        } catch (NoSuchFileException e) {
                            if (SRC_ROOT != null) {
                                File srcExpectedFile = new File(expectedFile.getAbsolutePath().replace(ROOT.getAbsolutePath(), SRC_ROOT));
                                Files.write(srcExpectedFile.toPath(), actual.getBytes());
                                fail("Expectations file not found and created: " + expectedFile.getAbsolutePath());
                            }
                            throw e;
                        }

                        try {
                            assertXMLEqual(expected, actual);
                        } catch (AssertionFailedError e) {
                            if (UPDATE && SRC_ROOT != null) {
                                File srcExpectedFile = new File(expectedFile.getAbsolutePath().replace(ROOT.getAbsolutePath(), SRC_ROOT));
                                Files.write(srcExpectedFile.toPath(), actual.getBytes());
                            }
                            throw e;
                        }
                    }
                });
            }
        }

        return suite;
    }
}
