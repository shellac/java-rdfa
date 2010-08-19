/*
 * (c) Copyright 2010 University of Bristol
 * All rights reserved.
 * [See end of file]
 */

package net.rootdev.javardfa;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;
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
        }
        assertTrue(result);
    }

    // Currently failing. charset issue
    @Ignore
    @Test
    public void t2() throws RepositoryException, IOException, RDFParseException, RDFHandlerException {
        load(expected, "/1.ttl", RDFFormat.TURTLE);
        //load(got, "/1.ttl", RDFFormat.TURTLE);
        load(got, "/1.xhtml", RDFaHtmlParserFactory.rdfa_html_Format);
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

/*
 * (c) Copyright 2009 University of Bristol
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