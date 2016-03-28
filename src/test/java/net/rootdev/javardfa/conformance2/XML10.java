package net.rootdev.javardfa.conformance2;

import org.apache.jena.rdf.model.Model;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import net.rootdev.javardfa.ParserFactory;
import net.rootdev.javardfa.ParserFactory.Format;
import net.rootdev.javardfa.jena.JenaStatementSink;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author pldms
 */
public class XML10 extends RDFaConformance
{
    @Parameters
    public static Collection<String[]> testFiles()
            throws URISyntaxException, IOException {
        return RDFaConformance.
                testFiles("http://rdfa.info/test-suite/rdfa1.0/xml/manifest",
                    "conformance2/manifest-extract.rq",
                    // Exclude: test uses html base outside html. Badly migrated, I guess.
                    "http://rdfa.info/test-suite/rdfa1.0/xml/0210",
                    // Exclude: test namespaces are wrong. xhtml isn't mentioned, dc inclusion is dubious.
                    "http://rdfa.info/test-suite/rdfa1.0/xml/0212"
                    
                );
    }

    public XML10(String test, String title,
            String purpose, String input, String query, String expected) {
        super(test, title, purpose, input, query, expected);
    }

    @Override
    public XMLReader getParser(Model model) throws SAXException {
        return ParserFactory.createReaderForFormat(new JenaStatementSink(model), Format.XHTML);
    }
}
