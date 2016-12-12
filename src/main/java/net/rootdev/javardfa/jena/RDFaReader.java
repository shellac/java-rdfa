/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa.jena;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFErrorHandler;
import org.apache.jena.rdf.model.RDFReader;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import net.rootdev.javardfa.Parser;
import net.rootdev.javardfa.ParserFactory;
import net.rootdev.javardfa.Setting;
import net.rootdev.javardfa.StatementSink;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Damian Steer <pldms@mac.com>
 */

public class RDFaReader implements RDFReader {

    static {
        RDFReaderFImpl.setBaseReaderClassName("HTML", HTMLRDFaReader.class.getName());
        RDFReaderFImpl.setBaseReaderClassName("XHTML", XHTMLRDFaReader.class.getName());
    }

    public static class HTMLRDFaReader extends RDFaReader {
        @Override public XMLReader getReader() {
            return ParserFactory.createHTML5Reader();
        }

        @Override public void initParser(Parser parser) {
            parser.enable(Setting.ManualNamespaces);
        }
    }

    public static class XHTMLRDFaReader extends RDFaReader {
        @Override public XMLReader getReader() throws SAXException {
            return ParserFactory.createNonvalidatingReader();
        }
    }

    private XMLReader xmlReader;

    public void read(Model arg0, Reader arg1, String arg2) {
        this.runParser(arg0, arg2, new InputSource(arg1));
    }

    public void read(Model arg0, InputStream arg1, String arg2) {
        this.runParser(arg0, arg2, new InputSource(arg1));
    }

    public void read(Model arg0, String arg1) {
        this.runParser(arg0, arg1, new InputSource(arg1));
    }

    public Object setProperty(String arg0, Object arg1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public RDFErrorHandler setErrorHandler(RDFErrorHandler arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setReader(XMLReader reader) { this.xmlReader = reader; }
    public XMLReader getReader() throws SAXException { return xmlReader; }
    public void initParser(Parser parser) { }

    private StatementSink getSink(Model arg0) {
        return new JenaStatementSink(arg0);
    }

    private void runParser(Model arg0, String arg2, InputSource source) {
        StatementSink sink = getSink(arg0);
        Parser parser = new Parser(sink);
        parser.setBase(arg2);
        initParser(parser);
        try {
            XMLReader xreader = getReader();
            xreader.setContentHandler(parser);
            xreader.parse(source);
        } catch (IOException ex) {
            throw new RuntimeException("IO Error when parsing", ex);
        } catch (SAXException ex) {
            throw new RuntimeException("SAX Error when parsing", ex);
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
