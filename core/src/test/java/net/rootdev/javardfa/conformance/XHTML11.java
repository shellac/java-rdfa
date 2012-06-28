/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa.conformance;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import net.rootdev.javardfa.JenaStatementSink;
import net.rootdev.javardfa.ParserFactory;
import net.rootdev.javardfa.ParserFactory.Format;
import net.rootdev.javardfa.Setting;

import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.hp.hpl.jena.rdf.model.Model;

/**
 *
 * @author pldms
 */
public class XHTML11 extends RDFaConformance
{
    @Parameters
    public static Collection<String[]> testFiles()
            throws URISyntaxException, IOException {
        return RDFaConformance.
                testFiles("http://rdfa.digitalbazaar.com/test-suite/test-cases/xhtml11/xhtml11-manifest.rdf");
    }

    public XHTML11(String test, String title,
            String purpose, String input, String query, String expected) {
        super(test, title, purpose, input, query, expected);
    }

    @Override
    public XMLReader getParser(Model model) throws SAXException {
    	return ParserFactory.createReaderForFormat(new JenaStatementSink(model), Format.XHTML, Setting.OnePointOne);
    }
}
