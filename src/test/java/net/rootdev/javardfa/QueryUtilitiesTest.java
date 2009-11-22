/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.sse.SSE;
import java.net.URL;
import java.util.Map;
import net.rootdev.javardfa.ParserFactory.Format;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pldms
 */
public class QueryUtilitiesTest {

    public QueryUtilitiesTest() {
    }

    /**
     * Test of makeQueries method, of class QueryUtilities.
     */
    @Test
    public void testMakeQueries() throws Exception {
        Map<String, Query> q =
                QueryUtilities.makeQueries(Format.XHTML, "/query-tests/1.html");
        assertEquals("Just the one query", 1, q.entrySet().size());
        String qId = q.entrySet().iterator().next().getKey();
        assertTrue("Got the right id for the query", qId.endsWith("/query-tests/1.html#1"));
        System.out.println("Query:");
        SSE.write(Algebra.compile(q.get(qId)));
        URL u = this.getClass().getResource("/query-tests/1.rq");
        Query exp = QueryFactory.read(u.toExternalForm());
        System.out.println("QueryE:");
        SSE.write(Algebra.compile(exp));
        assertEquals("Got the right query", Algebra.compile(exp), Algebra.compile(q.get(qId)));
    }

    /**
     * Test of extractBinding method, of class QueryUtilities.
     */
    @Test
    public void testExtractBinding() {
        
    }

}