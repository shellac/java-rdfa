/*
 * (c) 2009
 * Damian Steer <mailto:pldms@mac.com>
 */
package net.rootdev.javardfa;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.LinkedList;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;
import nu.validator.htmlparser.sax.HtmlParser;


/**
 *
 * @author pldms
 */
@RunWith(Parameterized.class)
public class RDFaInHTML {

    final static Logger log = LoggerFactory.getLogger(RDFaInHTML.class);
    final static String ManifestURI =
            "http://philip.html5.org/demos/rdfa/tests.json";

    public static class HTMLTest {
        String input;
        String expected;
        String purpose;
        String type;
        String id;
    }

    @Parameters
    public static Collection<HTMLTest[]> testFiles()
            throws URISyntaxException, IOException {

        FileManager fm = FileManager.get();

        Gson gson = new Gson();

        InputStream in = fm.open(ManifestURI);
        Reader reader = new InputStreamReader(in, "UTF-8");

        Type collectionType = new TypeToken<Collection<HTMLTest>>() {
        }.getType();
        Collection<HTMLTest> tests = gson.fromJson(reader, collectionType);
        reader.close();
        in.close();

        // Infelicity in junit
        Collection<HTMLTest[]> toReturn = new LinkedList<HTMLTest[]>();
        for (HTMLTest test : tests) {
            toReturn.add(new HTMLTest[]{test});
        }

        return toReturn;
    }
    private final HTMLTest test;
    private final XMLInputFactory xmlFactory;

    public RDFaInHTML(HTMLTest test) {
        this.test = test;
        xmlFactory = XMLInputFactory.newInstance();
        /* If you want it to go slowwwwww */
        xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    }

    @Ignore
    @Test
    public void compare() throws XMLStreamException, IOException, URISyntaxException {
        Model model = ModelFactory.createDefaultModel();
        StatementSink sink = new JenaStatementSink(model);

        Reader sreader = new StringReader(test.input);

        Source source = new SAXSource(new HtmlParser(), new InputSource(sreader));
        //source.setSystemId("");

        XMLEventReader reader = xmlFactory.createXMLEventReader(source);
        Parser parser = new Parser(reader, sink);
        try {
            parser.parse("http://example.com/");
        } catch (NullPointerException e) {
            fail("NPE <" + test + ">");
        }
        String actual = test.expected.replaceAll("<>", "<http://example.com/>");
        Model expectedModel = ModelFactory.createDefaultModel();
        expectedModel.read(new StringReader(test.expected), null, "N-TRIPLE");
        boolean result = expectedModel.isIsomorphicWith(model);
        if (!result) {
            System.err.println("------ " + test + " ------");
            model.write(System.err, "TTL");
            System.err.println("------ Expected ------");
            expectedModel.write(System.err, "TTL");
            System.err.println("-----------------------");
        }
        assertTrue(test.id + " (" + test.purpose + ")", result);
    }
}
