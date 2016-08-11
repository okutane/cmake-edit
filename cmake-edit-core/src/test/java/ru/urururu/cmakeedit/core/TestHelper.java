package ru.urururu.cmakeedit.core;

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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created by okutane on 11/08/16.
 */
class TestHelper {
    private static final double TESTS_THRESHOLD;
    private static final File ROOT;
    private static String SRC_ROOT = System.getProperty("SRC_ROOT");
    private static boolean UPDATE = Boolean.getBoolean("AUTO_UPDATE_ON_DIFF");

    static {
        URL url = ParserIntegrationTests.class.getResource("/");
        try {
            ROOT = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }

        double candidate;
        String testsThreshold = System.getProperty("INTEGRATION_TESTS_THRESHOLD");
        if (testsThreshold == null) {
            candidate = 1d;
        } else {
            try {
                candidate = Double.parseDouble(testsThreshold);
                if (candidate >= 0d && candidate < 1d) {
                    // can't invert due to bug in intellij.  ¯\_(ツ)_/¯
                } else {
                    candidate = 1d;
                }
            } catch (NumberFormatException e) {
                candidate = 1d;
            }
        }
        TESTS_THRESHOLD = candidate;
    }

    static TestSuite buildPack(String path, Function<FileNode, ?> conversion) {
        URL url = ParserIntegrationTests.class.getResource(path);
        File packRoot;
        try {
            packRoot = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }

        MetricRegistry registry = new MetricRegistry();

        TestSuite pack = createSuite(packRoot, registry, conversion);

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

    private static TestSuite createSuite(File file, MetricRegistry registry, Function<FileNode, ?> conversion) {
        TestSuite suite = new TestSuite(file.getName());

        for (File child : file.listFiles()) {
            if (child.getName().endsWith(".xml")) {
                continue;
            }

            if (child.isDirectory()) {
                suite.addTest(createSuite(child, registry, conversion));
            } else if (shouldAdd()) {
                File source = child;
                suite.addTest(new XMLTestCase(source.getName()) {
                    @Override
                    protected void runTest() throws Throwable {
                        String text = new String(Files.readAllBytes(source.toPath()), StandardCharsets.UTF_8);

                        text = text.replace("\r", "");

                        FileNode result = Parser.parse(new StringParseContext(text, 0, registry));

                        XStream xstream = new XStream(new Sun14ReflectionProvider(
                                new FieldDictionary(new ImmutableFieldKeySorter())),
                                new DomDriver("utf-8"));

                        String actual = xstream.toXML(conversion.apply(result));

                        File expectedFile = new File(source.getAbsolutePath() + ".xml");
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

    private static boolean shouldAdd() {
        return ThreadLocalRandom.current().nextDouble() <= TESTS_THRESHOLD;
    }
}
