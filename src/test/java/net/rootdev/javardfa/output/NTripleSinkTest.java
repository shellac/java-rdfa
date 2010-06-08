/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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