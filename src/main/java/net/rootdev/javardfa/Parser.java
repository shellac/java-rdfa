/*
 * (c) 2009
 * Damian Steer <mailto:pldms@mac.com>
 */

package net.rootdev.javardfa;

import com.hp.hpl.jena.graph.Node;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author pldms
 */
public class Parser
{
    private final XMLEventReader reader;
    private final StatementSink sink;

    public Parser(XMLEventReader reader, StatementSink sink)
    {
        this.reader = reader;
        this.sink = sink;
    }

    public void parse(String base) throws XMLStreamException
    {
        try {
            sink.start();
            EvalContext context = new EvalContext(base);
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement())
                    parse(context, (StartElement) event);
            }
        } finally {
            reader.close();
            sink.end();
        }
    }

    void parse(EvalContext context, StartElement element) throws XMLStreamException
    {
        Iterator attributes = element.getAttributes();
        while (attributes.hasNext())
            handleAttribute(context, (Attribute) attributes.next());

        /* Hackity hack! */
        /* if input hack hack hack */
        if (element.getName().getLocalPart().equals("input")) {
            
        }

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement())
                parse(context.copy(), (StartElement) event);
            if (event.isEndDocument() || event.isEndElement())
                return;
        }
    }

    void handleAttribute(EvalContext context, Attribute attr)
    {
        if (attr.getName().getLocalPart().equals("about"))
            context.parentSubject = attr.getValue();
        if (attr.getName().getLocalPart().equals("property"))
            context.properties.add(attr.getValue());
        if (attr.getName().getLocalPart().equals("resource"))
            emitTriplesWithObject(context, attr.getValue());
    }

    void emitTriplesWithObject(EvalContext context, String object)
    {
        Node subjectN = Node.createURI(context.parentSubject);
        Node objectN = Node.createURI(object);
        for (String prop: context.properties)
            sink.add(subjectN, Node.createURI(prop), objectN);
    }

    static class EvalContext
    {
        String base;
        String parentSubject;
        String parentObject;
        List<String> properties;
        Map<String, String> uriMappings;

        public EvalContext(String base) {
            this.base = base;
            this.parentSubject = base;
            this.properties = new LinkedList<String>();
        }

        private EvalContext(String base, String parentSubject, String parentObject) {
            this.base = base;
            this.parentSubject = parentSubject;
            this.parentObject = parentObject;
            this.properties = new LinkedList<String>();
        }

        public EvalContext copy() {
            return new EvalContext(base, parentSubject, parentObject);
        }
    }
}
