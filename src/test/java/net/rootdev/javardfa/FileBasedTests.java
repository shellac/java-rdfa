/*
 * (c) 2009
 * Damian Steer <mailto:pldms@mac.com>
 */
package net.rootdev.javardfa;

import com.hp.hpl.jena.graph.Node;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author pldms
 */
@RunWith(Parameterized.class)
public class FileBasedTests {

    @Parameters
    public static Collection<String[]> testFiles()
            throws URISyntaxException, IOException {
        InputStream stream = FileBasedTests.class.getClassLoader().getResourceAsStream("test-manifest");
        if (stream == null) {
            throw new Error("Couldn't find test-manifest");
        }
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        Collection<String[]> filePairs = new ArrayList<String[]>();
        String nextLine;
        while ((nextLine = reader.readLine()) != null) {
            filePairs.add(nextLine.split("\\s+"));
        }
        return filePairs;
    }
    private final String htmlFile;
    private final String queryFile;
    private final XMLInputFactory xmlFactory;

    public FileBasedTests(String htmlFile, String queryFile) {
        this.htmlFile = htmlFile;
        this.queryFile = queryFile;
        xmlFactory = XMLInputFactory.newInstance();
    }

    @Test
    public void compare() throws XMLStreamException, IOException, URISyntaxException {
        InputStream htmlIn =
                this.getClass().getClassLoader().getResourceAsStream(htmlFile);
        XMLEventReader reader = xmlFactory.createXMLEventReader(htmlIn);
        StatementCollector coll = new StatementCollector();
        Parser parser = new Parser(reader, coll);
        parser.enable(Parser.Setting.FormMode);
        parser.parse("http://example.com/" + htmlFile);
        assertTrue(htmlFile + " and " + queryFile + " are the same",
                htmlFile.equals(queryFile));
    }

    static class StatementCollector implements StatementSink {

        List<Node[]> statements = new ArrayList<Node[]>();

        //@Override
        public void start() {
        }

        //@Override
        public void end() {
        }

        //@Override
        public void addObject(String subject, String predicate, String object) {
            System.err.printf("<%s> <%s> <%s>\n", subject, predicate, object);
        }

        //@Override
        public void addLiteral(String subject, String predicate, String lex, String lang, String datatype) {
            if (lang == null && datatype == null) {
                System.err.printf("<%s> <%s> \"%s\"\n", subject, predicate, lex);
            } else if (lang != null) {
                System.err.printf("<%s> <%s> \"%s\"@%s\n", subject, predicate, lex, lang);
            } else {
                System.err.printf("<%s> <%s> \"%s\"^^%s\n", subject, predicate, lex, datatype);
            }
        }
    }
}
