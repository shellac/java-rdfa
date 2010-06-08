/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pldms
 */
public class SimpleProfileCollectorTest {

    public SimpleProfileCollectorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of getProfile method, of class SimpleProfileCollector.
     */
    @Test
    public void testGetProfile() {
        String profileURI = SimpleProfileCollectorTest.class.getResource("/profile.xhtml").toExternalForm();
        // Fix java stupidity -- again
        profileURI = profileURI.replaceFirst("^file:/(?!/)", "file:///");
        EvalContext context = new EvalContext("http://example.com/base");
        ProfileCollector instance = new SimpleProfileCollector();
        instance.getProfile(profileURI, context);
        assertEquals("http://xmlns.com/foaf/0.1/", context.getURIForPrefix("foaf"));
        assertEquals("http://example.com/rabbit", context.getURIForTerm("rabbit"));
    }

}