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
import org.junit.Test;
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
import nu.validator.htmlparser.common.XmlViolationPolicy;
import org.xml.sax.InputSource;
import nu.validator.htmlparser.sax.HtmlParser;
import org.xml.sax.SAXException;


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
            if (!test.purpose.contains("Scripted"))
                toReturn.add(new HTMLTest[]{test});
        }

        return toReturn;
    }
    private final HTMLTest test;

    public RDFaInHTML(HTMLTest test) {
        this.test = test;
    }

    //@Ignore
    @Test
    public void compare() throws SAXException, IOException, Throwable {
        Model model = ModelFactory.createDefaultModel();
        StatementSink sink = new JenaStatementSink(model);
        Reader sreader = new StringReader(test.input);
        Parser parser = new Parser(sink);
        parser.setBase("http://example.com/");
        parser.enable(Parser.Setting.ManualNamespaces);
        HtmlParser reader = new HtmlParser();
        reader.setXmlPolicy(XmlViolationPolicy.ALLOW);
        reader.setXmlnsPolicy(XmlViolationPolicy.ALLOW);
        //reader.setMappingLangToXmlLang(true);
        reader.setContentHandler(parser);
        Throwable error = null;
        try {
            reader.parse(new InputSource(sreader));
        } catch (Throwable e) {
            error = e;
        }
        String actual = test.expected.replaceAll("<>", "<http://example.com/>");
        actual = actual.replaceAll("\\{BASE\\}", "http://example.com/");
        Model expectedModel = ModelFactory.createDefaultModel();
        expectedModel.read(new StringReader(actual), null, "N-TRIPLE");
        boolean result = expectedModel.isIsomorphicWith(model);
        if (!result) {
            System.err.println("------ " + test.id + " ------");
            model.write(System.err, "TTL");
            System.err.println("------ Input ------");
            System.err.println(test.input);
            System.err.println("------ Expected ------");
            expectedModel.write(System.err, "TTL");
            System.err.println("-----------------------");
        }
        if (error != null) 
            throw new Error("Error in " + test.id + "(" + error.getMessage()
                    + ")", error);
        assertTrue(test.id + " (" + test.purpose + ")", result);
    }
}
