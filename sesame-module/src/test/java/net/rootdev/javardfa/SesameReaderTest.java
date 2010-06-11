/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import org.junit.Test;
import org.junit.Before;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import static org.junit.Assert.*;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.repository.util.RepositoryUtil;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author pldms
 */
public class SesameReaderTest {
    private SailRepository got;
    private SailRepository expected;

    public SesameReaderTest() {
    }

    @Before
    public void setUp() throws SailException, RepositoryException, IOException, RDFParseException {
        got = new SailRepository(new MemoryStore());
        got.initialize();
        expected = new SailRepository(new MemoryStore());
        expected.initialize();
    }

    @Test
    public void t1() throws RepositoryException, IOException, RDFParseException, RDFHandlerException {
        load(expected, "/1.ttl", RDFFormat.TURTLE);
        //load(got, "/1.ttl", RDFFormat.TURTLE);
        load(got, "/1.xhtml", RDFaXHtmlParserFactory.rdfa_xhtml_Format);
        boolean result = RepositoryUtil.equals(expected, got);
        if (!result) {
            Collection<? extends Statement> x = RepositoryUtil.difference(expected, got);
            for (Statement s: x) {
                System.err.println(s);
            }
            System.err.println("===");
            x = RepositoryUtil.difference(got, expected);
            for (Statement s: x) {
                System.err.println(s);
            }
            got.getConnection().export(new TurtleWriter(System.err), (Resource) null);
        }
        assertTrue(result);
    }

    private void load(SailRepository rep, String file, RDFFormat format) throws RepositoryException, IOException, RDFParseException {
        SailRepositoryConnection conn = rep.getConnection();
        URL url = this.getClass().getResource(file);

        conn.add(url, 
                url.toExternalForm().replaceFirst("^file:/(?!/)", "file:///"),
                format, (Resource) null);
    }
}