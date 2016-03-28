/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */

package net.rootdev.javardfa;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.util.FileManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import net.rootdev.javardfa.ParserFactory.Format;
import net.rootdev.javardfa.jena.JenaStatementSink;
import net.rootdev.javardfa.query.QueryUtilities;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Damian Steer <pldms@mac.com>
 */

@RunWith(Parameterized.class)
public class FileBasedTests {

    static final String BASE = "/query-tests/";

    @Parameters
    public static Collection<String[]> testFiles()
            throws URISyntaxException, IOException {
        InputStream stream = FileBasedTests.class.getClassLoader().getResourceAsStream("test-manifest");
        if (stream == null) {
            throw new Error("Couldn't find test-manifest");
        }
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        Collection<String[]> filePairs = new ArrayList<String[]>();
        String nextLine;
        while ((nextLine = reader.readLine()) != null) {
            if (nextLine.startsWith("#")) continue;
            filePairs.add(nextLine.split("\\s+"));
        }
        return filePairs;
    }
    private final String htmlFile;
    private final String compareFile;
    private final XMLInputFactory xmlFactory;

    public FileBasedTests(String htmlFile, String compareFile) {
        this.htmlFile = BASE + htmlFile;
        this.compareFile = BASE + compareFile;
        xmlFactory = XMLInputFactory.newInstance();
    }

    @Test
    public void compare() throws XMLStreamException, IOException, ParserConfigurationException, SAXException {
        URL htmlURL = this.getClass().getResource(htmlFile);
        URL compareURL = this.getClass().getResource(compareFile);
        
        if (compareFile.endsWith(".rq")) compareQuery(htmlURL, compareURL);
        else compareRDF(htmlURL, compareURL);
    }

    private void compareRDF(URL htmlURL, URL compareURL) throws SAXException, IOException {
        String cf = compareURL.toExternalForm();
        if (cf.matches("file:/[^/][^/].*")) cf = cf.replaceFirst("file:/", "file:///");
        String hf = htmlURL.toExternalForm();
        if (hf.matches("file:/[^/][^/].*")) hf = hf.replaceFirst("file:/", "file:///");
        Model c = FileManager.get().loadModel(compareURL.toExternalForm());
        Model m = ModelFactory.createDefaultModel();
        StatementSink sink = new JenaStatementSink(m);
        XMLReader parser = ParserFactory.createReaderForFormat(sink, Format.XHTML, Setting.OnePointOne);
        parser.parse(hf);
        boolean result = c.isIsomorphicWith(m);
        if (!result) m.write(System.err, "TTL");
        assertTrue("Files match (" + htmlURL + ")", result);
    }

    private void compareQuery(URL htmlURL, URL compareURL) throws SAXException, IOException {
        Query query = QueryFactory.read(compareURL.toExternalForm());
        
        Map<String, Query> qs = QueryUtilities.makeQueries(ParserFactory.Format.XHTML,
                htmlURL.toExternalForm());

        assertTrue("We have a query", qs.size() != 0);

        Query qFromHTML = qs.get(qs.keySet().toArray()[0]);

        assertEquals("Query matches (" + htmlURL + ")",
                Algebra.compile(query),
                Algebra.compile(qFromHTML));
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
