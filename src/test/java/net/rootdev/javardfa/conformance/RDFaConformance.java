/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa.conformance;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
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

/**
 * @author Damian Steer <pldms@mac.com>
 */
@RunWith(Parameterized.class)
public abstract class RDFaConformance {

    final static Logger log = LoggerFactory.getLogger(RDFaConformance.class);
    
    public static Collection<String[]> testFiles(String manifestURI, String... excludes)
            throws URISyntaxException, IOException {

        Set<String> toExclude = new HashSet(Arrays.asList(excludes));

        FileManager fm = FileManager.get();

        Model manifest = fm.loadModel(manifestURI);

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
            if (toExclude.contains(params[0]) ||
                    toExclude.contains(params[3]) ||
                    toExclude.contains(params[4]) ) {
                log.warn("Skipping test <" + params[0] + ">");
                continue;
            }
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
    }

    public abstract XMLReader getParser(Model model) throws SAXException;

    @Test
    public void compare() throws SAXException, IOException {
        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(input);
        XMLReader reader = getParser(model);
        try {
            InputSource ins = new InputSource(in);
            ins.setEncoding("utf-8");
            ins.setSystemId(input);
            reader.parse(ins);
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
