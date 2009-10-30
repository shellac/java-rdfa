/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import java.io.OutputStream;
import java.util.logging.Level;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pldms
 */
public class RDFXMLSink implements StatementSink {
    final static Logger log = LoggerFactory.getLogger(RDFXMLSink.class);
    final static String RDFNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    private final XMLStreamWriter out;

    public RDFXMLSink(OutputStream os) {
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
}
