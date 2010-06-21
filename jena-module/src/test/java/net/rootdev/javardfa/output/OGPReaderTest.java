/*
 * (c) Copyright 2010 University of Bristol
 * All rights reserved.
 * [See end of file]
 */

package net.rootdev.javardfa.output;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import net.rootdev.javardfa.ParserFactory.Format;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pldms
 */
public class OGPReaderTest {
    
    /**
     * Test of getOGP method, of class OGPReader.
     */
    @Test
    public void testGetOGP() throws Exception {
        URL toLoad = OGPReaderTest.class.getResource("/ogp.html");

        System.out.println("getOGP");
        String url = "";
        Format format = null;
        Map<String, String> expResult = new HashMap<String, String>();
        expResult.put("latitude", "37.416343");
        expResult.put("longitude", "-122.153013");
        expResult.put("street-address", "1601 S California Ave");
        expResult.put("locality", "Palo Alto");
        expResult.put("region", "CA");
        expResult.put("postal-code", "94304");
        expResult.put("country-name", "USA");
        expResult.put("http://example.com/ex#foo", "not-ogp");
        Map<String, String> result = OGPReader.getOGP(toLoad.toExternalForm(), Format.HTML);
        assertEquals(expResult, result);
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