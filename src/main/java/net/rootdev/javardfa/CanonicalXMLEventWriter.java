/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */

package net.rootdev.javardfa;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * All this class does currently is ensure the order of attributes, but
 * (By using a stream writer) it is more controllable than the event writer.
 *
 * @author pldms
 */
public class CanonicalXMLEventWriter 
        implements XMLEventWriter {
    private final XMLStreamWriter swriter;

    public CanonicalXMLEventWriter(XMLStreamWriter swriter) {
        this.swriter = swriter;
    }

    public void flush() throws XMLStreamException {
        swriter.flush();
    }

    public void close() throws XMLStreamException {
        swriter.close();
    }

    public void add(XMLEvent event) throws XMLStreamException {
        if (event.isEndElement()) {
            swriter.writeEndElement();
        } else if (event.isCharacters()) {
            swriter.writeCharacters(event.asCharacters().getData());
        } else if (event.isProcessingInstruction()) {
            swriter.writeProcessingInstruction(((ProcessingInstruction) event).getData(),
                    ((ProcessingInstruction) event).getTarget());
        } else if (event.isStartElement()) {
            StartElement se = event.asStartElement();
            swriter.writeStartElement(se.getName().getPrefix(),
                    se.getName().getLocalPart(),
                    se.getName().getNamespaceURI());
            writeAttributes(se);
        } else {
            System.err.printf("Gah! Missed one <%s>, '%s'", event.getClass(), event);
        }
    }

    public void add(XMLEventReader reader) throws XMLStreamException {
        while (reader.hasNext()) this.add(reader.nextEvent());
    }

    public String getPrefix(String uri) throws XMLStreamException {
        return swriter.getPrefix(uri);
    }

    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        swriter.setPrefix(prefix, uri);
    }

    public void setDefaultNamespace(String uri) throws XMLStreamException {
        swriter.setDefaultNamespace(uri);
    }

    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        swriter.setNamespaceContext(context);
    }

    public NamespaceContext getNamespaceContext() {
        return swriter.getNamespaceContext();
    }

    private void writeAttributes(StartElement se) throws XMLStreamException {
        SortedMap<String, Attribute> atts = new TreeMap<String, Attribute>();
        for (Iterator i = se.getAttributes(); i.hasNext();) {
            Attribute a = (Attribute) i.next();
            atts.put(getName(a), a);
        }
        for (Attribute a: atts.values()) {
            swriter.writeAttribute(
                    a.getName().getPrefix(),
                    a.getName().getNamespaceURI(),
                    a.getName().getLocalPart(),
                    a.getValue());
        }
    }

    private String getName(Attribute a) {
        QName name = a.getName();
        String toReturn = (name.getPrefix() == null) ?
            name.getLocalPart() : name.getPrefix() + ":" + name.getLocalPart();
        if (toReturn.startsWith("xml:")) return "_" + toReturn;
        else return toReturn;
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