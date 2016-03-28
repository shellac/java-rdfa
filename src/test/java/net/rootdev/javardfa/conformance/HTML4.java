/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa.conformance;

import org.apache.jena.rdf.model.Model;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import net.rootdev.javardfa.jena.JenaStatementSink;
import net.rootdev.javardfa.ParserFactory;
import net.rootdev.javardfa.ParserFactory.Format;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author pldms
 */
public class HTML4 extends RDFaConformance
{
    @Parameters
    public static Collection<String[]> testFiles()
            throws URISyntaxException, IOException {
        if (false)
            return Collections.EMPTY_LIST;
        else
        return RDFaConformance.
                testFiles("http://rdfa.digitalbazaar.com/test-suite/test-cases/html4/html4-manifest.rdf",
                "http://rdfa.digitalbazaar.com/test-suite/test-cases/html4/0121",
                "http://rdfa.digitalbazaar.com/test-suite/test-cases/html4/0113"
                );
    }

    public HTML4(String test, String title,
            String purpose, String input, String query, String expected) {
        super(test, title, purpose, input, query, expected);
    }

    @Override
    public XMLReader getParser(Model model) throws SAXException {
        return ParserFactory.createReaderForFormat(new JenaStatementSink(model), Format.HTML);
    }
}
