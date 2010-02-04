/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import net.rootdev.javardfa.ParserFactory.Format;
import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.XMLReader;

/**
 * Note: most testing happens in conformance
 * @author pldms
 */
public class ParserTest {

    public ParserTest() {
    }

    /**
     * This bit me. html5 parser sets systemId rather late.
     */
    @Test
    public void testBaseLocator() throws Exception {
        Model m = ModelFactory.createDefaultModel();
        StatementSink sink = new JenaStatementSink(m);
        XMLReader parser = ParserFactory.createReaderForFormat(sink, Format.HTML);
        parser.parse(this.getClass().getResource("/simple.html").toExternalForm());
        Resource r = m.listSubjects().nextResource();
        assertTrue("HTML 5 base correct", r.getURI().endsWith("simple.html"));
    }

}