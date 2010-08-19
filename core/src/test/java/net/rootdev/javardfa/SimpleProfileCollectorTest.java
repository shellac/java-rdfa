/*
 * (c) Copyright 2010 University of Bristol
 * All rights reserved.
 * [See end of file]
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
        assertEquals("http://xmlns.com/foaf/0.2/", context.getURIForPrefix("foaf2"));
        assertEquals("http://example.com/Rabbit", context.getURIForTerm("Rabbit"));
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