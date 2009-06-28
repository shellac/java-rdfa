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
import javax.xml.namespace.QName;
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

    final QName about = new QName("about"); // safe
    final QName resource = new QName("resource"); // safe
    final QName href = new QName("href"); // URI
    final QName src = new QName("src"); // URI
    final QName property = new QName("property"); // CURIE
    final QName datatype = new QName("datatype"); // CURIE
    final QName typeof = new QName("typeof"); // CURIE
    final QName rel = new QName("rel"); // Link types and CURIES
    final QName rev = new QName("rev"); // Link type and CURIES

    // Hack bits
    final QName input = new QName("input");
    final QName name = new QName("name");

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
        boolean recurse = true;
        boolean skipElement = false;
        String newSubject = null;
        String currentObject = null;
        Map<String, String> uriMappings = context.uriMappings;
        List incompleteTriples = null;
        String currentLanguage = context.language;

        // TODO element.getNamespace();
        // TODO element.getAttribute (xmlLang)

        if (element.getAttributeByName(rev) == null &&
                element.getAttributeByName(rel) == null) {
            Attribute nSubj = findAttribute(element, about, src, resource, href);
            if (nSubj != null) newSubject = nSubj.getValue();
            else {
                // TODO if element is head or body assume about=""
                if (element.getAttributeByName(typeof) != null)
                    newSubject = "_:bnode"; // TODO unique
                else {
                    if (context.parentObject != null) newSubject = context.parentObject;
                    if (element.getAttributeByName(property) == null) skipElement = true;
                }
            }
        } else {
            Attribute nSubj = findAttribute(element, about, src);
            if (nSubj != null) newSubject = nSubj.getValue();
            else {
                // TODO if element is head or body assume about=""
                if (element.getAttributeByName(typeof) != null)
                    newSubject = "_:bnode"; // TODO unique
                else {
                    if (context.parentObject != null) newSubject = context.parentObject;
                }
            }
            Attribute cObj = findAttribute(element, resource, href);
            if (cObj != null) currentObject = cObj.getValue();
        }

        if (newSubject != null && element.getAttributeByName(typeof) != null)
            emitTriple(newSubject,
                    "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
                    element.getAttributeByName(typeof).getValue());

        if (currentObject != null) {
            if (element.getAttributeByName(rel) != null)
                emitTriple(newSubject,
                        element.getAttributeByName(rel).getValue(),
                        currentObject);
            if (element.getAttributeByName(rev) != null)
                emitTriple(currentObject,
                        element.getAttributeByName(rev).getValue(),
                        newSubject);
        }

        // TODO incomplete triples

        // TODO step 9 literals

        if (!skipElement && newSubject != null) {
            // complete incomplete -- TODO direction

            for (String prop: context.properties) {
                emitTriple(context.parentSubject,
                        prop,
                        newSubject);
            }
        }

        if (recurse) {
            EvalContext ec = context.copy();
            // TODO stuff
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement())
                    parse(ec, (StartElement) event);
                if (event.isEndDocument() || event.isEndElement())
                    return;
            }
        }

        Iterator attributes = element.getAttributes();
        while (attributes.hasNext())
            handleAttribute(context, (Attribute) attributes.next());

        /* Hackity hack! */
        /* if input hack hack hack */
        if (element.getName().equals(input)) {
            Attribute theName = element.getAttributeByName(name);
            if (theName != null) emitTriplesWithObject(context, theName.getValue());
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
        QName attrName = attr.getName();
        if (attrName.equals(about))
            context.parentSubject = attr.getValue();
        if (attrName.equals(property))
            context.properties.add(attr.getValue());
        if (attrName.equals(resource))
            emitTriplesWithObject(context, attr.getValue());
    }

    void emitTriplesWithObject(EvalContext context, String object)
    {
        Node subjectN = Node.createURI(context.parentSubject);
        Node objectN = Node.createURI(object);
        for (String prop: context.properties)
            sink.add(subjectN, Node.createURI(prop), objectN);
    }

    private Attribute findAttribute(StartElement element, QName... names)
      {
        for (QName aName: names) {
            Attribute a = element.getAttributeByName(name);
            if (a != null) return a;
        }
        return null;
      }

    private void emitTriple(String newSubject, String string, String value)
      {
        throw new UnsupportedOperationException("Not yet implemented");
      }

    static class EvalContext
    {
        String base;
        String parentSubject;
        String parentObject;
        List<String> properties;
        Map<String, String> uriMappings;
        String language;

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
