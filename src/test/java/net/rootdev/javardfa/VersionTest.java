/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pldms
 */
public class VersionTest {

    /**
     * Test of get method, of class Version.
     */
    @Test
    public void testGet() {
        assertNotNull(Version.get());
    }

    /**
     * Test of getName method, of class Version.
     */
    @Test
    public void testGetName() {
        assertEquals("java-rdfa", Version.get().getName());
    }

    /**
     * Test of getVersion method, of class Version.
     */
    @Test
    public void testGetVersion() {
        // Gah, can't usefully test this
    }

    /**
     * Test of toString method, of class Version.
     */
    @Test
    public void testToString() {
        assertTrue(Version.get().toString().matches("java-rdfa (.*)"));
    }

}