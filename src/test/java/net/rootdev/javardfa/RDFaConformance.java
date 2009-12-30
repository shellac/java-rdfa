/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Damian Steer <pldms@mac.com>
 */
@RunWith(Parameterized.class)
public class RDFaConformance {

    final static Logger log = LoggerFactory.getLogger(RDFaConformance.class);
    final static String ManifestURI =
            "http://www.w3.org/2006/07/SWD/RDFa/testsuite/xhtml1-testcases/rdfa-xhtml1-test-manifest.rdf";

    @Parameters
    public static Collection<String[]> testFiles()
            throws URISyntaxException, IOException {

        FileManager fm = FileManager.get();

        Model manifest = fm.loadModel(ManifestURI);

        Query manifestExtract = QueryFactory.read("manifest-extract.rq");

        Collection<String[]> tests = new ArrayList<String[]>();

        QueryExecution qe = QueryExecutionFactory.create(manifestExtract, manifest);

        ResultSet results = qe.execSelect();

        if (!results.hasNext()) {
            throw new RuntimeException("No results");
        }
        while (results.hasNext()) {

            QuerySolution soln = results.next();
            String[] params = new String[6];
            params[0] = soln.getResource("test").getURI();
            params[1] = soln.getLiteral("title").getString();
            params[2] = soln.getLiteral("purpose").getString();
            params[3] = soln.getResource("input").getURI();
            params[4] = soln.getResource("query").getURI();
            // getBoolean not working??
            //boolean expected = (soln.contains("expect")) ?
            //    soln.getLiteral("expect").getBoolean() : true;
            params[5] = soln.contains("expect") ? soln.getLiteral("expect").getLexicalForm() : "true";
            tests.add(params);
        }

        return tests;
    }
    private final String test;
    private final String title;
    private final String purpose;
    private final String input;
    private final String query;
    private final boolean expected;

    public RDFaConformance(String test, String title,
            String purpose, String input, String query, String expected) {
        this.test = test;
        this.title = title;
        this.purpose = purpose;
        this.input = input;
        this.query = query;
        this.expected = Boolean.valueOf(expected);
        /* If you want it to go slowwwwww */
    }

    @Test
    public void compare() throws SAXException, IOException {
        Model model = ModelFactory.createDefaultModel();
        StatementSink sink = new JenaStatementSink(model);
        InputStream in = FileManager.get().open(input);
        XMLReader reader = XMLReaderFactory.createXMLReader();
        Parser parser = new Parser(
                sink,
                XMLOutputFactory.newInstance(),
                XMLEventFactory.newInstance(),
                new IRIResolver()
                );
        parser.setBase(input);
        reader.setContentHandler(parser);
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        try {
            reader.parse(new InputSource(in));
        } catch (NullPointerException e) {
            fail("NPE <" + test + ">");
        }
        Query theQuery = QueryFactory.read(query);
        QueryExecution qe = QueryExecutionFactory.create(theQuery, model);
        boolean result = qe.execAsk();
        if (result != expected) {
            System.err.println("------ " + test + " ------");
            model.write(System.err, "TTL");
            System.err.println("------ Query ------");
            System.err.println(theQuery);
            System.err.println("-----------------------");
        }
        if (expected) {
            assertTrue(title + " <" + test + ">", result);
        } else {
            assertFalse(title + " <" + test + ">", result);
        }
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
