/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import org.json.JSONException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import org.xml.sax.InputSource;
import nu.validator.htmlparser.sax.HtmlParser;
import org.xml.sax.SAXException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Damian Steer <pldms@mac.com>
 */
@RunWith(Parameterized.class)
public class RDFaInHTML {

    final static Logger log = LoggerFactory.getLogger(RDFaInHTML.class);
    final static String ManifestURI =
            "http://philip.html5.org/demos/rdfa/tests.json";
    final static String Base = "http://www.example.com/BASE";

    public static class HTMLTest {

        String input;
        String expected;
        String purpose;
        String type;
        String id;
    }

    @Parameters
    public static Collection<HTMLTest[]> testFiles()
            throws URISyntaxException, IOException, JSONException {

        FileManager fm = FileManager.get();

        InputStream in = fm.open(ManifestURI);

        String jsonData = fm.readWholeFileAsUTF8(in);

        // I want GSON back. Stupid maven!
        //Collection<HTMLTest> tests = gson.fromJson(reader, collectionType);

        // Infelicity in junit
        Collection<HTMLTest[]> toReturn = new LinkedList<HTMLTest[]>();

        JSONArray allTests = new JSONArray(jsonData);
        for (int i = 0; i < allTests.length(); i++) {
            JSONObject obj = allTests.getJSONObject(i);
            HTMLTest test = new HTMLTest();
            test.expected = obj.getString("expected");
            test.id = obj.getString("id");
            test.input = obj.getString("input");
            test.purpose = obj.getString("purpose");
            test.type = obj.getString("type");
            if (!test.purpose.contains("Scripted"))
                toReturn.add(new HTMLTest[]{test});
        }

        return toReturn;
    }
    private final HTMLTest test;

    public RDFaInHTML(HTMLTest test) {
        this.test = test;
    }

    //@Ignore
    @Test
    public void compare() throws SAXException, IOException, Throwable {
        Model model = ModelFactory.createDefaultModel();
        StatementSink sink = new JenaStatementSink(model);
        Reader sreader = new StringReader(test.input);
        Parser parser = new Parser(sink);
        parser.setBase(Base);
        parser.enable(Parser.Setting.ManualNamespaces);
        HtmlParser reader = new HtmlParser();
        reader.setXmlPolicy(XmlViolationPolicy.ALLOW);
        reader.setXmlnsPolicy(XmlViolationPolicy.ALLOW);
        reader.setMappingLangToXmlLang(false);
        reader.setContentHandler(parser);
        Throwable error = null;
        try {
            reader.parse(new InputSource(sreader));
        } catch (Throwable e) {
            error = e;
        }
        String actual = test.expected.replaceAll("<>", "<" + Base + ">");
        actual = actual.replaceAll("\\{BASE\\}", Base);
        Model expectedModel = ModelFactory.createDefaultModel();
        expectedModel.read(new StringReader(actual), null, "N-TRIPLE");
        boolean result = expectedModel.isIsomorphicWith(model);
        if (!result) {
            System.err.println("------ " + test.id + " ------");
            model.write(System.err, "TTL");
            System.err.println("------ Input ------");
            System.err.println(test.input);
            System.err.println("------ Expected ------");
            expectedModel.write(System.err, "TTL");
            System.err.println("-----------------------");
        }
        if (error != null) {
            throw new Error("Error in " + test.id + "(" + error.getMessage() + ")", error);
        }
        assertTrue(test.id + " (" + test.purpose + ")", result);
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
