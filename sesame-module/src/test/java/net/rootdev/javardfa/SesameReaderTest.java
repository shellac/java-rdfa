/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import java.io.IOException;
import java.net.URL;
import org.junit.Before;
import org.openrdf.model.Graph;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author pldms
 */
public class SesameReaderTest {

    public SesameReaderTest() {
    }

    @Before
    public void setUp() throws SailException, RepositoryException, IOException, RDFParseException {
        Repository myRepository = new SailRepository(new MemoryStore());
        myRepository.initialize();
        RepositoryConnection conn = myRepository.getConnection();
        conn.add((URL) null, null, RDFaHtmlParserFactory.rdfa_html_Format);
        Graph myGraph = new org.openrdf.model.impl.GraphImpl();
        
    }
}