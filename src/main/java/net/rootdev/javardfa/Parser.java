/*
 * (c) 2009
 * Damian Steer <mailto:pldms@mac.com>
 */

package net.rootdev.javardfa;

import com.hp.hpl.jena.graph.Node;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

    // Suggestion: switch this for object produced by factory that matches QNames
    // we can then en-slacken if needed by passing in different factory etc

    final QName about = new QName("about"); // safe
    final QName resource = new QName("resource"); // safe
    final QName href = new QName("href"); // URI
    final QName src = new QName("src"); // URI
    final QName property = new QName("property"); // CURIE
    final QName datatype = new QName("datatype"); // CURIE
    final QName typeof = new QName("typeof"); // CURIE
    final QName rel = new QName("rel"); // Link types and CURIES
    final QName rev = new QName("rev"); // Link type and CURIES
    final QName content = new QName("content");

    // Hack bits
    final QName input = new QName("input");
    final QName name = new QName("name");

    final Collection<String> rdfType = Collections.singleton("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

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
                    parse(context, event.asStartElement());
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
        List<String> forwardProperties = new LinkedList(context.forwardProperties);
        List<String> backwardProperties = new LinkedList(context.backwardProperties);
        String currentLanguage = context.language;

        // TODO element.getNamespace();
        // TODO element.getAttribute (xmlLang)

        if (element.getAttributeByName(rev) == null &&
                element.getAttributeByName(rel) == null) {
            Attribute nSubj = findAttribute(element, about, src, resource, href);
            if (nSubj != null) newSubject = getURI(element, nSubj);
            else {
                // TODO if element is head or body assume about=""
                if (element.getAttributeByName(typeof) != null)
                    newSubject = createBNode();
                else {
                    if (context.parentObject != null) newSubject = context.parentObject;
                    if (element.getAttributeByName(property) == null) skipElement = true;
                }
            }
        } else {
            Attribute nSubj = findAttribute(element, about, src);
            if (nSubj != null) newSubject = getURI(element, nSubj);
            else {
                // TODO if element is head or body assume about=""
                if (element.getAttributeByName(typeof) != null)
                    newSubject = createBNode();
                else {
                    if (context.parentObject != null) newSubject = context.parentObject;
                }
            }
            Attribute cObj = findAttribute(element, resource, href);
            if (cObj != null) currentObject = getURI(element, cObj);
        }

        if (newSubject != null && element.getAttributeByName(typeof) != null) {
            List<String> types = getURIs(element, element.getAttributeByName(typeof));
            for (String type: types)
                emitTriples(newSubject,
                    rdfType,
                    type);
        }

        if (currentObject != null) {
            if (element.getAttributeByName(rel) != null)
                emitTriples(newSubject,
                        getURIs(element, element.getAttributeByName(rel)),
                        currentObject);
            if (element.getAttributeByName(rev) != null)
                emitTriples(currentObject,
                        getURIs(element, element.getAttributeByName(rev)),
                        newSubject);
        } else {
            if (element.getAttributeByName(rel) != null)
                forwardProperties.addAll(getURIs(element, element.getAttributeByName(rel)));
            if (element.getAttributeByName(rev) != null)
                backwardProperties.addAll(getURIs(element, element.getAttributeByName(rev)));
            if (element.getAttributeByName(rel) != null || // if predicate present
                    element.getAttributeByName(rev) != null)
                currentObject = createBNode(); // TODO generate bnode
        }

        if (element.getAttributeByName(property) != null) {
            List<String> props = getURIs(element, element.getAttributeByName(property));

            // TODO dataypes and XMLLiterals

            if (element.getAttributeByName(content) != null) {
                emitTriplesLiteral(newSubject,
                        props,
                        element.getAttributeByName(content).getValue());
            } else { // TODO this is wrong
                StringBuilder value = new StringBuilder();
                getPlainLiteralValue(value);
                emitTriplesLiteral(newSubject,
                        props,
                        value.toString());
            }
        }

        if (!skipElement && newSubject != null) {
            emitTriples(context.parentSubject,
                        context.forwardProperties,
                        newSubject);

            emitTriples(newSubject,
                        context.backwardProperties,
                        context.parentSubject);
        }

        if (recurse) {
            EvalContext ec = new EvalContext(context);

            if (skipElement) {
                //ec.language = language;
                //copy uri mappings
                System.err.println("Skip element");
            } else {
                if (newSubject != null)
                    ec.parentSubject = newSubject;
                else
                    ec.parentSubject = context.parentSubject;

                if (currentObject != null)
                    ec.parentObject = currentObject;
                else if (newSubject != null)
                    ec.parentObject = newSubject;
                else
                    ec.parentObject = context.parentSubject;

                ec.uriMappings = uriMappings;
                //ec.language = language
                ec.forwardProperties = forwardProperties;
                ec.backwardProperties = backwardProperties;
            }

            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                //System.err.println("Event: " + event);
                //System.err.println("Context: " + ec);
                if (event.isStartElement())
                    parse(ec, event.asStartElement());
                if (event.isEndDocument() || event.isEndElement())
                    return;
            }
        }
    }

    private Attribute findAttribute(StartElement element, QName... names)
      {
        for (QName aName: names) {
            Attribute a = element.getAttributeByName(aName);
            if (a != null) return a;
        }
        return null;
      }

    private void emitTriples(String subj, Collection<String> props, String obj)
      {
        for (String prop: props)
            sink.add(Node.createURI(subj), Node.createURI(prop), Node.createURI(obj));
      }

    private void emitTriplesLiteral(String subj, Collection<String> props, String obj)
    {
        for (String prop: props)
            sink.add(Node.createURI(subj), Node.createURI(prop), Node.createLiteral(obj));
    }

    private void getPlainLiteralValue(StringBuilder value)
            throws XMLStreamException
      {
        XMLEvent event = reader.nextEvent();
        while (!event.isEndElement()) {
            if (event.isCharacters()) value.append(event.asCharacters().getData());
            if (event.isStartElement()) {
                getPlainLiteralValue(value); // TODO this is wrong!!
            }
            event = reader.nextEvent();
        }
      }

    private String getURI(StartElement element, Attribute attr) {
        QName attrName = attr.getName();
        if (attrName.equals(href) || attrName.equals(src)) // A URI
            return attr.getValue();
        if (attrName.equals(about) || attrName.equals(resource)) // Safe CURIE or URI
            return expandSafeCURIE(element, attr.getValue());
        if (attrName.equals(datatype)) // A CURIE
            return expandCURIE(element, attr.getValue());
        throw new RuntimeException("Unexpected attribute: " + attr);
    }

    private List<String> getURIs(StartElement element, Attribute attr) {
        List<String> uris = new LinkedList<String>();
        String[] curies = attr.getValue().split("\\s+");
        for (String curie: curies)
            uris.add(expandCURIE(element, curie));
        return uris;
    }

    int bnodeId = 0;

    private String createBNode() // TODO probably broken? Can you write bnodes in rdfa directly?
      {
        return "_:node" + (bnodeId++);
      }

    private String expandCURIE(StartElement element, String value)
      {
        int offset = value.indexOf(":");
        if (offset < 1) throw new RuntimeException("Is this a curie? \"" + value + "\"");
        String prefix = value.substring(0, offset);
        String namespaceURI = element.getNamespaceURI(prefix);
        if (namespaceURI == null) throw new RuntimeException("Unknown prefix: " + prefix);
        return namespaceURI + value.substring(offset + 1);
      }

    private String expandSafeCURIE(StartElement element, String value)
      {
        if (value.startsWith("[") && value.endsWith("]"))
            return expandCURIE(element, value.substring(1, value.length() - 1));
        else
            return value;
      }

    static class EvalContext
    {
        String base;
        String parentSubject;
        String parentObject;
        Map<String, String> uriMappings;
        String language;
        List<String> forwardProperties;
        List<String> backwardProperties;

        private EvalContext(String base) {
            this.base = base;
            this.parentSubject = base;
            this.forwardProperties = new LinkedList<String>();
            this.backwardProperties = new LinkedList<String>();
            this.uriMappings = new HashMap<String, String>();
        }

        public EvalContext(EvalContext toCopy) {
            this.base = toCopy.base;
            this.parentSubject = toCopy.parentSubject;
            this.parentObject = toCopy.parentObject;
            this.uriMappings = new HashMap<String, String>(toCopy.uriMappings);
            this.language = toCopy.language;
            this.forwardProperties = new LinkedList<String>(toCopy.forwardProperties);
            this.backwardProperties = new LinkedList<String>(toCopy.backwardProperties);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[\nbase: " + base);
            sb.append("\nparentSubject: " + parentSubject);
            sb.append("\nparentObject: " + parentObject);
            sb.append("\nforward: [");
            for (String prop: forwardProperties) {
                sb.append(prop);
                sb.append(" ");
            }
            sb.append("]");
            return sb.toString();
        }
    }
}
