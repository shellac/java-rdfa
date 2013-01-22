/*
 * (c) Copyright 2010 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa.literal;

import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import net.rootdev.javardfa.Parser;
import net.rootdev.javardfa.Setting;

/**
 *
 * @author pldms
 */
public class LiteralCollector {

    final String XMLLiteral = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";

    private final Stack<Collector> collectors;
    private List<XMLEvent> queuedEvents;
    private int level;
    private final Parser parser;
    private final StartElement fakeEnvelope;
    private final XMLEventFactory eventFactory;
    private final XMLOutputFactory outputFactory;

    public LiteralCollector(Parser parser, XMLEventFactory eventFactory, XMLOutputFactory outputFactory) {
        this.parser = parser;
        this.collectors = new Stack<Collector>();
        this.queuedEvents = null;
        this.eventFactory = eventFactory;
        this.outputFactory = outputFactory;
        this.fakeEnvelope = eventFactory.createStartElement(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI, "fake");
    }

    public boolean isCollecting() { return !collectors.isEmpty(); }

    public boolean isCollectingXML() {
        if (!isCollecting()) return false;
        return XMLLiteral.equals(collectors.peek().datatype);
    }

    public void collect(String subject, Collection<String> props, String datatype, String lang) {
        if (!isCollecting()) { // set up collection
            queuedEvents = new LinkedList<XMLEvent>();
            level = 0;
        }

        Collector coll = new Collector(subject, props, datatype, lang, level, queuedEvents.size());
        collectors.push(coll);
    }

    public void handleEvent(XMLEvent event) {
        if (!isCollecting()) return; // nothing to do
        if (event.isStartElement()) handleStartEvent(event);
        else if (event.isEndElement()) handleEndEvent(event);
        else queuedEvents.add(event);
    }

    private void handleStartEvent(XMLEvent event) {
        level++;
        queuedEvents.add(event);
        // In 1.0 if no explicit dt given dt determined by content
        // i.e. if it contains tags we have an xml literal
        if (!parser.isEnabled(Setting.OnePointOne) &&
            collectors.peek().datatype == null) { // undecided so far
            collectors.peek().datatype = XMLLiteral;
        }
    }

    private void handleEndEvent(XMLEvent event) {
        queuedEvents.add(event);
        if (collectors.peek().level == level) { 
            Collector coll = collectors.pop();
            emitTriples(coll, queuedEvents.subList(coll.start, queuedEvents.size()));
        }
        level--;
    }

    private void emitTriples(Collector coll, List<XMLEvent> subList) {
        String lex = (XMLLiteral.equals(coll.datatype)) ?
            gatherXML(subList, coll.lang) :
            gatherText(subList) ;
        if ((coll.datatype != null) && !"".equals(coll.datatype)) // not plain
            parser.emitTriplesDatatypeLiteral(coll.subject,
                    coll.props, lex, coll.datatype);
        else
            parser.emitTriplesPlainLiteral(coll.subject,
                    coll.props, lex, coll.lang);
    }

    private String gatherXML(List<XMLEvent> subList, String lang) {
        try {
            return gatherXMLEx(subList, lang);
        } catch (XMLStreamException ex) {
            throw new RuntimeException("Problem gathering XML", ex);
        }
    }

    private String gatherXMLEx(List<XMLEvent> subList, String lang)
            throws XMLStreamException {
        Attribute xmlLang = (lang == null) ?
            null :
            eventFactory.createAttribute("xml:lang", lang);
        StringWriter sw = new StringWriter();
        XMLStreamWriter out = outputFactory.createXMLStreamWriter(sw);
        XMLEventWriter xmlWriter = new CanonicalXMLEventWriter(out, xmlLang);
        xmlWriter.add(fakeEnvelope); // Some libraries dislike xml fragements
        for (XMLEvent e: subList) {
            xmlWriter.add(e);
        }
        xmlWriter.flush();
        String xml = sw.toString();
        int start = xml.indexOf('>') + 1;
        int end = xml.lastIndexOf('<');
        return xml.substring(start, end); // remove <fake ...></fake>
    }

    private String gatherText(List<XMLEvent> subList) {
        StringBuilder sb = new StringBuilder();
        for (XMLEvent e: subList) {
            if (e.isCharacters()) sb.append(e.asCharacters().getData());
        }
        return sb.toString();
    }

    final static class Collector {
        private final String subject;
        private final Collection<String> props;
        private String datatype;
        private final String lang;
        private final int level;
        private final int start;

        private Collector(String subject, Collection<String> props, String datatype,
                String lang, int level, int start) {
            this.subject = subject;
            this.props = props;
            this.datatype = datatype;
            this.lang = lang;
            this.level = level;
            this.start = start;
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