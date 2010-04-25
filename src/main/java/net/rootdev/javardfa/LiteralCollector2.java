/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author pldms
 */
public class LiteralCollector2 {

    private final Stack<Collector> collectors;
    private List<XMLEvent> queuedEvents;
    private int level;
    private final Parser parser;
    private final StartElement fakeEnvelope;

    public LiteralCollector2(Parser parser) {
        this.parser = parser;
        this.collectors = new Stack<Collector>();
        this.queuedEvents = null;
        this.fakeEnvelope = parser.eventFactory.createStartElement("", null, "fake");
    }

    public boolean isCollecting() { return !collectors.isEmpty(); }

    public boolean isCollectingXML() {
        if (!isCollecting()) return false;
        return parser.consts.xmlLiteral.equals(collectors.peek().datatype);
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
        if (collectors.peek().datatype == null) { // undecided so far
            collectors.peek().datatype = parser.consts.xmlLiteral;
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
        String lex = (parser.consts.xmlLiteral.equals(coll.datatype)) ?
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
            parser.eventFactory.createAttribute("xml:lang", lang);
        StringWriter sw = new StringWriter();
        XMLStreamWriter out = parser.outputFactory.createXMLStreamWriter(sw);
        XMLEventWriter xmlWriter = new CanonicalXMLEventWriter(out, xmlLang);
        xmlWriter.add(fakeEnvelope); // Some libraries dislike xml fragements
        for (XMLEvent e: subList) {
            xmlWriter.add(e);
        }
        xmlWriter.flush();
        String xml = sw.toString();
        return xml.substring(6, xml.length() - 7); // remove <fake></fake>
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
