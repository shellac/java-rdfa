/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */

package net.rootdev.javardfa;

import com.hp.hpl.jena.graph.Node;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Damian Steer <pldms@mac.com>
 */

@RunWith(Parameterized.class)
public class FileBasedTests {

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
            filePairs.add(nextLine.split("\\s+"));
        }
        return filePairs;
    }
    private final String htmlFile;
    private final String queryFile;
    private final XMLInputFactory xmlFactory;

    public FileBasedTests(String htmlFile, String queryFile) {
        this.htmlFile = htmlFile;
        this.queryFile = queryFile;
        xmlFactory = XMLInputFactory.newInstance();
    }

    @Ignore
    @Test
    public void compare() throws XMLStreamException, IOException, ParserConfigurationException, SAXException {
        InputStream htmlIn =
                this.getClass().getClassLoader().getResourceAsStream(htmlFile);
        //XMLEventReader reader = xmlFactory.createXMLEventReader(htmlIn);
        XMLReader reader = XMLReaderFactory.createXMLReader();
        StatementCollector coll = new StatementCollector();
        Parser parser = new Parser(coll);
        parser.enable(Parser.Setting.FormMode);
        //parser.parse("http://example.com/" + htmlFile);

        reader.setContentHandler(parser);
        reader.parse(new InputSource(htmlIn));

        assertTrue(htmlFile + " and " + queryFile + " are the same",
                htmlFile.equals(queryFile));
    }

    static class StatementCollector implements StatementSink {

        List<Node[]> statements = new ArrayList<Node[]>();

        //@Override
        public void start() {
        }

        //@Override
        public void end() {
        }

        //@Override
        public void addObject(String subject, String predicate, String object) {
            System.err.printf("<%s> <%s> <%s>\n", subject, predicate, object);
        }

        //@Override
        public void addLiteral(String subject, String predicate, String lex, String lang, String datatype) {
            if (lang == null && datatype == null) {
                System.err.printf("<%s> <%s> \"%s\"\n", subject, predicate, lex);
            } else if (lang != null) {
                System.err.printf("<%s> <%s> \"%s\"@%s\n", subject, predicate, lex, lang);
            } else {
                System.err.printf("<%s> <%s> \"%s\"^^%s\n", subject, predicate, lex, datatype);
            }
        }

        public void addPrefix(String prefix, String uri) {
            System.err.printf("PREFIX %s: <%s>\n", prefix, uri);
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
