package net.rootdev.javardfa.conformance2;

import org.apache.jena.rdf.model.Model;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import net.rootdev.javardfa.ParserFactory;
import net.rootdev.javardfa.ParserFactory.Format;
import net.rootdev.javardfa.Setting;
import net.rootdev.javardfa.jena.JenaStatementSink;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author pldms
 */
public class XML11 extends RDFaConformance
{
    @Parameters
    public static Collection<String[]> testFiles()
            throws URISyntaxException, IOException {
        if (false) return RDFaConformance.filterTests(RDFaConformance.
                testFiles("http://rdfa.info/test-suite/rdfa1.1/xml/manifest",
                "conformance2/manifest-extract-1.1.rq"
                ), 120);
        return RDFaConformance.
                testFiles("http://rdfa.info/test-suite/rdfa1.1/xml/manifest",
                "conformance2/manifest-extract-1.1.rq"
                );
    }

    public XML11(String test, String title,
            String purpose, String input, String query, String expected) {
        super(test, title, purpose, input, query, expected);
    }

    @Override
    public XMLReader getParser(Model model) throws SAXException {
        return ParserFactory.createReaderForFormat(new JenaStatementSink(model), Format.XHTML, Setting.OnePointOne);
    }
}
