/*
 * (c) Copyright 2010 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa.output;

import java.io.StringWriter;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pldms
 */
public class NTripleSinkTest {
    private StringWriter out;
    private NTripleSink sink;

    public NTripleSinkTest() {
    }

    @Before
    public void setUp() {
        this.out = new StringWriter();
        this.sink = new NTripleSink(out);
    }

    /**
     * Test of end method, of class NTripleSink.
     */
    @Test
    public void testEnd() {}

    /**
     * Test of addObject method, of class NTripleSink.
     */
    @Test
    public void testAddObject() {}

    /**
     * Test of addLiteral method, of class NTripleSink.
     */
    @Test
    public void testAddLiteral() {}

    /**
     * Test of addPrefix method, of class NTripleSink.
     */
    @Test
    public void testAddPrefix() {}

    /**
     * Test of toNode method, of class NTripleSink.
     */
    @Test
    public void testToNode() {
        assertEquals("Convert bnode", "_:a ", sink.toNode("_:a"));
        assertEquals("Convert var", "?a ", sink.toNode("?a"));
        assertEquals("Convert uri", "<http://www.example.com/> ", sink.toNode("http://www.example.com/"));
    }

    /**
     * Test of toLiteral method, of class NTripleSink.
     */
    @Test
    public void testToLiteral() {
        assertEquals("Convert plain", "\"a\" ", sink.toLiteral("a", null, null));
        assertEquals("Convert lang", "\"a\"@en ", sink.toLiteral("a", "en", null));
        assertEquals("Convert no lang", "\"a\" ", sink.toLiteral("a", "", null));
        assertEquals("Convert dt", "\"a\"^^<http://www.example.com/> ", sink.toLiteral("a", null, "http://www.example.com/"));
    }

    /**
     * Test of quote method, of class NTripleSink.
     */
    @Test
    public void testQuote() {
        assertEquals("Quote me", "\"a\\\"b\"", sink.quote("a\"b"));
        assertEquals("Quote tab", "\"a\\tb\"", sink.quote("a\tb"));
        assertEquals("Quote newline", "\"a\\nb\"", sink.quote("a\nb"));
        assertEquals("Quote non-ascii", "\"a\\ufeffb\"", sink.quote("a\ufeffb"));
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