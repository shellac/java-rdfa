/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */

package net.rootdev.javardfa.output;

import java.io.OutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import net.rootdev.javardfa.StatementSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A pretty ropey RDF/XML serialiser.
 * Advantages: streams, no dependencies.
 *
 * @author pldms
 */
public class RDFXMLSink implements StatementSink {
    final static Logger log = LoggerFactory.getLogger(RDFXMLSink.class);
    final static String RDFNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    private final XMLStreamWriter out;
    private final String[] comments;

    public RDFXMLSink(OutputStream os, String... comments) {
        this.comments = comments;
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        try {
            out = factory.createXMLStreamWriter(os, "utf-8");
        } catch (XMLStreamException ex) {
            throw new RuntimeException("Couldn't create writer", ex);
        }
    }

    public void start() {
        try {
            out.writeStartDocument("utf-8", "1.0");
            if (comments.length != 0) {
                out.writeCharacters("\n");
                StringBuilder sb = new StringBuilder("\n");
                for (String line: comments) { sb.append(line); sb.append("\n"); }
                out.writeComment(sb.toString());
            }
            out.writeCharacters("\n");
            out.writeStartElement(RDFNS, "RDF");
            out.writeNamespace("rdf", RDFNS);
            out.writeCharacters("\n");
        } catch (XMLStreamException ex) {
            throw new RuntimeException("Problem starting document", ex);
        }
    }

    public void end() {
        try {
            out.writeEndDocument();
            out.flush();
            out.close();
        } catch (XMLStreamException ex) {
            throw new RuntimeException("Problem ending document", ex);
        }
    }

    public void addObject(String subject, String predicate, String object) {
        try {
            out.writeStartElement(RDFNS, "Description");
            writeSubject(subject);
            out.writeCharacters("\n\t");
            writePredicate(predicate, true); // closed
            writeObject(object);
            out.writeCharacters("\n");
            out.writeEndElement();
            out.writeCharacters("\n");
        } catch (XMLStreamException ex) {
            throw new RuntimeException("Problem writing statement", ex);
        }
    }

    public void addLiteral(String subject, String predicate, String lex, String lang, String datatype) {
        try {
            out.writeStartElement(RDFNS, "Description");
            writeSubject(subject);
            out.writeCharacters("\n\t");
            writePredicate(predicate, false); // not closed
            writeLiteral(lex, lang, datatype);
            out.writeEndElement();
            out.writeCharacters("\n");
            out.writeEndElement();
            out.writeCharacters("\n");
        } catch (XMLStreamException ex) {
            throw new RuntimeException("Problem writing statement", ex);
        }
    }

    public void addPrefix(String prefix, String uri) {
    }

    private void writeSubject(String subject) throws XMLStreamException {
        if (blank(subject))
            out.writeAttribute(RDFNS, "nodeID", id(subject));
        else
            out.writeAttribute(RDFNS, "about", subject);
    }

    private void writePredicate(String predicate, boolean closed) throws XMLStreamException {
        String[] nsln = split(predicate);
        if (closed)
            out.writeEmptyElement("ns", nsln[1], nsln[0]);
        else
            out.writeStartElement("ns", nsln[1], nsln[0]);
        out.writeNamespace("ns", nsln[0]);
    }

    private void writeObject(String object) throws XMLStreamException {
        if (blank(object))
            out.writeAttribute(RDFNS, "nodeID", id(object));
        else
            out.writeAttribute(RDFNS, "resource", object);
    }

    private void writeLiteral(String lex, String lang, String datatype) throws XMLStreamException {
        if (lang != null)
            out.writeAttribute("xml:lang", lang);
        else if (datatype != null)
            out.writeAttribute(RDFNS, "datatype", datatype);
        out.writeCharacters(lex);
    }

    private boolean blank(String subject) {
        return subject.startsWith("_:");
    }

    private String id(String subject) {
        return subject.substring(2);
    }

    protected String[] split(String predicate) {
        String[] toReturn = new String[2];
        int i = predicate.length() - 1;
        int lastStartChar = -1;
        while (i > 0 && isNameChar(predicate.codePointAt(i))) {
            if (isNameStartChar(predicate.codePointAt(i)))
                lastStartChar = i;
            i--;
        }
        if (lastStartChar == -1)
            throw new RuntimeException("Unsplitable predicate " + predicate);
        toReturn[0] = predicate.substring(0, lastStartChar);
        toReturn[1] = predicate.substring(lastStartChar);
        return toReturn;
    }

    private boolean isNameChar(int cp) {
        return isNameStartChar(cp) ||
                (cp == '.') ||
                (cp == '-') ||
                (cp >= '0' && cp <= '9');
    }

    private boolean isNameStartChar(int cp) {
        return (cp >= 'a' && cp <= 'z') ||
                (cp >= 'A' && cp <= 'Z') ||
                cp == ':' ||
                cp == '_';
    }

    public void setBase(String base) {}
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