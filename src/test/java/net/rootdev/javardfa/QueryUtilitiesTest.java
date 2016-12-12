/*
 * (c) Copyright 2010 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

import net.rootdev.javardfa.ParserFactory.Format;
import net.rootdev.javardfa.query.QueryUtilities;

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
                QueryUtilities.makeQueries(Format.XHTML,
                this.getClass().getResource("/query-tests/1.html").toExternalForm());
        assertEquals("Just the one query", 1, q.entrySet().size());
        String qId = q.entrySet().iterator().next().getKey();
        assertTrue("Got the right id for the query", qId.endsWith("/query-tests/1.html#1"));
        //System.out.println("Query:");
        SSE.write(Algebra.compile(q.get(qId)));
        URL u = this.getClass().getResource("/query-tests/1.rq");
        Query exp = QueryFactory.read(u.toExternalForm());
        //System.out.println("QueryE:");
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

/*
 * (c) Copyright 2010 University of Bristol
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */