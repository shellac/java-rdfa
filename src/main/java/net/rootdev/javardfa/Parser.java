/*
 * (c) 2009
 * Damian Steer <mailto:pldms@mac.com>
 */
package net.rootdev.javardfa;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 *
 * @author pldms
 */
public class Parser {

    final static List<String> _allowed = Arrays.asList(
            "alternate", "appendix", "bookmark", "cite",
            "chapter", "contents", "copyright", "first",
            "glossary", "help", "icon", "index", "last",
            "license", "meta", "next", "p3pv1", "prev",
            "collection", "role", "section", "stylesheet",
            "subsection", "start", "top", "up" );

    final static Set<String> SpecialRels = new HashSet<String>(_allowed);

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
    final QName lang = new QName("http://www.w3.org/XML/1998/namespace", "lang");
    final QName base = new QName("http://www.w3.org/1999/xhtml", "base");
    final QName head = new QName("http://www.w3.org/1999/xhtml", "head");
    final QName body = new QName("http://www.w3.org/1999/xhtml", "body");
    // Hack bits
    final QName input = new QName("input");
    final QName name = new QName("name");
    final Collection<String> rdfType = Collections.singleton("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    final String xmlLiteral = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";
    final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

    public Parser(XMLEventReader reader, StatementSink sink) {
        this.reader = reader;
        this.sink = sink;
    }

    public void parse(String base) throws XMLStreamException, IOException, URISyntaxException {
        try {
            sink.start();
            EvalContext context = new EvalContext(base);
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {
                    parse(context, event.asStartElement());
                }
            }
        } finally {
            reader.close();
            sink.end();
        }
    }

    //private String currentBase;

    void parse(EvalContext context, StartElement element) throws XMLStreamException, IOException, URISyntaxException {
        boolean recurse = true;
        boolean skipElement = false;
        String newSubject = null;
        String currentObject = null;
        //Map<String, String> uriMappings = context.uriMappings;
        List<String> forwardProperties = new LinkedList(context.forwardProperties);
        List<String> backwardProperties = new LinkedList(context.backwardProperties);
        String currentLanguage = context.language;
        String currentBase = context.base;

        // TODO element.getNamespace();

        if (element.getAttributeByName(lang) != null)
            currentLanguage = element.getAttributeByName(lang).getValue();

        if (base.equals(element.getName()) && element.getAttributeByName(href) != null) {
            currentBase = getBase(element.getAttributeByName(href));
            context.base = currentBase;
            if (context.original) context.parentSubject = currentBase;
        }

        if (element.getAttributeByName(rev) == null &&
                element.getAttributeByName(rel) == null) {
            Attribute nSubj = findAttribute(element, about, src, resource, href);
            if (nSubj != null) {
                newSubject = getURI(currentBase, element, nSubj);
            } else {
                if (element.getAttributeByName(typeof) != null) {
                    if (body.equals(element.getName()) ||
                            head.equals(element.getName()))
                        newSubject = currentBase;
                    else
                        newSubject = createBNode();
                } else {
                    if (context.parentObject != null) {
                        newSubject = context.parentObject;
                    }
                    if (element.getAttributeByName(property) == null) {
                        skipElement = true;
                    }
                }
            }
        } else {
            Attribute nSubj = findAttribute(element, about, src);
            if (nSubj != null) {
                newSubject = getURI(currentBase, element, nSubj);
            } else {
                // TODO if element is head or body assume about=""
                if (element.getAttributeByName(typeof) != null) {
                    newSubject = createBNode();
                } else {
                    if (context.parentObject != null) {
                        newSubject = context.parentObject;
                    }
                }
            }
            Attribute cObj = findAttribute(element, resource, href);
            if (cObj != null) {
                currentObject = getURI(currentBase, element, cObj);
            }
        }

        if (newSubject != null && element.getAttributeByName(typeof) != null) {
            List<String> types = getURIs(currentBase, element, element.getAttributeByName(typeof));
            for (String type : types) {
                emitTriples(newSubject,
                        rdfType,
                        type);
            }
        }

        if (newSubject == null) newSubject = context.parentSubject;

        if (currentObject != null) {
            if (element.getAttributeByName(rel) != null) {
                emitTriples(newSubject,
                        getURIs(currentBase, element, element.getAttributeByName(rel)),
                        currentObject);
            }
            if (element.getAttributeByName(rev) != null) {
                emitTriples(currentObject,
                        getURIs(currentBase, element, element.getAttributeByName(rev)),
                        newSubject);
            }
        } else {
            if (element.getAttributeByName(rel) != null) {
                forwardProperties.addAll(getURIs(currentBase, element, element.getAttributeByName(rel)));
            }
            if (element.getAttributeByName(rev) != null) {
                backwardProperties.addAll(getURIs(currentBase, element, element.getAttributeByName(rev)));
            }
            if (element.getAttributeByName(rel) != null || // if predicate present
                    element.getAttributeByName(rev) != null) {
                currentObject = createBNode(); // TODO generate bnode
            }
        }

        // Getting literal values. Complicated!
        if (element.getAttributeByName(property) != null) {
            List<String> props = getURIs(currentBase, element, element.getAttributeByName(property));
            String theDatatype = getDatatype(element);
            StringWriter lexVal = new StringWriter();
            boolean isPlain = false;
            if (theDatatype != null && !theDatatype.isEmpty() && !theDatatype.equals(xmlLiteral)) {
                // Datatyped literal
                if (element.getAttributeByName(content) != null) {
                    lexVal.append(element.getAttributeByName(content).getValue());
                } else {
                    getPlainLiteralValue(lexVal);
                    recurse = false;
                }
            } else {
                // Plain or XML
                if (element.getAttributeByName(content) != null) {
                    isPlain = true;
                    lexVal.append(element.getAttributeByName(content).getValue());
                } else if (theDatatype != null && theDatatype.isEmpty()) { // force plain
                    isPlain = true;
                    getPlainLiteralValue(lexVal);
                    recurse = false;
                } else {
                    isPlain = getLiteralValue(lexVal);
                    recurse = false;
                    if (!isPlain) {
                        theDatatype = xmlLiteral;
                    }
                }
            }
            lexVal.flush();
            String lexical = lexVal.toString();

            if (isPlain) {
                emitTriplesPlainLiteral(newSubject,
                        props,
                        lexical, currentLanguage);
            } else {
                emitTriplesDatatypeLiteral(newSubject,
                        props,
                        lexical, theDatatype);
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
                ec.language = currentLanguage;
                ec.original = context.original;
                //copy uri mappings
            } else {
                if (newSubject != null) {
                    ec.parentSubject = newSubject;
                } else {
                    ec.parentSubject = context.parentSubject;
                }

                if (currentObject != null) {
                    ec.parentObject = currentObject;
                } else if (newSubject != null) {
                    ec.parentObject = newSubject;
                } else {
                    ec.parentObject = context.parentSubject;
                }

                //ec.uriMappings = uriMappings;
                ec.language = currentLanguage;
                ec.forwardProperties = forwardProperties;
                ec.backwardProperties = backwardProperties;
            }

            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {
                    /*System.err.println("Continuing from " + element.getName().getLocalPart() + " to " + event.asStartElement().getName().getLocalPart());
                    System.err.println(ec);*/
                    parse(ec, event.asStartElement());
                    if (!currentBase.equals(ec.base)) { // bubbling up base change
                        // I could just let parentS = null, rather than bother with original?
                        context.base = ec.base;
                        if (context.original) context.parentSubject = ec.base;
                    }
                }
                if (event.isEndDocument() || event.isEndElement()) {
                    return;
                }
            }
        }
    }

    private Attribute findAttribute(StartElement element, QName... names) {
        for (QName aName : names) {
            Attribute a = element.getAttributeByName(aName);
            if (a != null) {
                return a;
            }
        }
        return null;
    }

    private void emitTriples(String subj, Collection<String> props, String obj) {
        for (String prop : props) {
            sink.addObject(subj, prop, obj);
        }
    }

    private void emitTriplesPlainLiteral(String subj, Collection<String> props, String lex, String language) {
        for (String prop : props) {
            sink.addLiteral(subj, prop, lex, language, null);
        }
    }

    private void emitTriplesDatatypeLiteral(String subj, Collection<String> props, String lex, String datatype) {
        for (String prop : props) {
            sink.addLiteral(subj, prop, lex, null, datatype);
        }
    }

    private void getPlainLiteralValue(Writer writer)
            throws XMLStreamException, IOException {
        int level = 0; // keep track of when we leave
        XMLEvent event = reader.nextEvent();
        while (!(event.isEndElement() && level == 0)) {
            if (event.isCharacters()) {
                writer.append(event.asCharacters().getData());
            }
            if (event.isStartElement()) {
                level++;
            }
            if (event.isEndElement()) {
                level--;
            }
            event = reader.nextEvent();
        }
    }

    /**
     *
     * @param writer Literal value will be written here
     * @return true if this is a plain literal
     * @throws XMLStreamException
     */
    private boolean getLiteralValue(Writer writer)
            throws XMLStreamException, IOException {
        List<Characters> queuedCharacters = new LinkedList<Characters>();
        XMLEvent event = reader.nextEvent();
        while (event.isCharacters()) {
            queuedCharacters.add(event.asCharacters());
            event = reader.nextEvent();
        }
        if (event.isEndElement()) { // All characters, plain literal
            for (Characters chars : queuedCharacters) {
                writer.append(chars.getData());
            }
            return true;
        }
        // We are an xml literal! Copy everything
        XMLEventWriter xwriter = outputFactory.createXMLEventWriter(writer);
        for (Characters chars : queuedCharacters) {
            xwriter.add(chars);
        }
        
        int level = 0; // keep track of when we leave
        while (!(event.isEndElement() && level == 0)) {
            xwriter.add(event);
            if (event.isStartElement()) {
                level++;
            }
            if (event.isEndElement()) {
                level--;
            }
            event = reader.nextEvent();
        }
        xwriter.close();
        return false;
    }

    private String getURI(String base, StartElement element, Attribute attr) throws URISyntaxException {
        QName attrName = attr.getName();
        if (attrName.equals(href) || attrName.equals(src)) // A URI
        {
            if (attr.getValue().isEmpty()) return base;
            URI uri = new URI(base);
            URI resolved = uri.resolve(attr.getValue());
            return resolved.toString();
        }
        if (attrName.equals(about) || attrName.equals(resource)) // Safe CURIE or URI
        {
            return expandSafeCURIE(base, element, attr.getValue());
        }
        if (attrName.equals(datatype)) // A CURIE
        {
            return expandCURIE(element, attr.getValue());
        }
        throw new RuntimeException("Unexpected attribute: " + attr);
    }

    private List<String> getURIs(String base, StartElement element, Attribute attr) {
        List<String> uris = new LinkedList<String>();
        String[] curies = attr.getValue().split("\\s+");
        for (String curie : curies) {
            if (SpecialRels.contains(curie))
                uris.add("http://www.w3.org/1999/xhtml/vocab#" + curie);
            else {
                String uri = expandCURIE(element, curie);
                if (uri != null) uris.add(uri);
            }
        }
        return uris;
    }
    
    int bnodeId = 0;

    private String createBNode() // TODO probably broken? Can you write bnodes in rdfa directly?
    {
        return "_:node" + (bnodeId++);
    }

    private String expandCURIE(StartElement element, String value) {
        if (value.startsWith("_:")) return value;
        int offset = value.indexOf(":");
        if (offset == -1) {
            //throw new RuntimeException("Is this a curie? \"" + value + "\"");
            return null;
        }
        String prefix = value.substring(0, offset);
        String namespaceURI = prefix.isEmpty() ?
            "http://www.w3.org/1999/xhtml/vocab#" :
            element.getNamespaceURI(prefix) ;
        if (namespaceURI == null) {
            throw new RuntimeException("Unknown prefix: " + prefix);
        }
        if (namespaceURI.endsWith("/") || namespaceURI.endsWith("#"))
            return namespaceURI + value.substring(offset + 1);
        else
            return namespaceURI + "#" + value.substring(offset + 1);
    }

    private String expandSafeCURIE(String base, StartElement element, String value) throws URISyntaxException {
        if (value.startsWith("[") && value.endsWith("]")) {
            return expandCURIE(element, value.substring(1, value.length() - 1));
        } else {
            if (value.isEmpty()) return base;
            URI uri = new URI(base);
            URI resolved = uri.resolve(value);
            return resolved.toString();
        }
    }

    private String getDatatype(StartElement element) {
        Attribute de = element.getAttributeByName(datatype);
        if (de == null) {
            return null;
        }
        String dt = de.getValue();
        if (dt.isEmpty()) {
            return dt;
        }
        return expandCURIE(element, dt);
    }

    private String getBase(Attribute attr) {
        String theBase = attr.getValue();
        if (theBase.contains("#"))
            return theBase.substring(0, theBase.indexOf("#"));
        else
            return theBase;
    }

    static class EvalContext {

        String base;
        String parentSubject;
        String parentObject;
        //Map<String, String> uriMappings;
        String language;
        List<String> forwardProperties;
        List<String> backwardProperties;
        boolean original;

        private EvalContext(String base) {
            this.base = base;
            this.parentSubject = base;
            this.forwardProperties = new LinkedList<String>();
            this.backwardProperties = new LinkedList<String>();
            //this.uriMappings = new HashMap<String, String>();
            original = true;
        }

        public EvalContext(EvalContext toCopy) {
            this.base = toCopy.base;
            this.parentSubject = toCopy.parentSubject;
            this.parentObject = toCopy.parentObject;
            //this.uriMappings = new HashMap<String, String>(toCopy.uriMappings);
            this.language = toCopy.language;
            this.forwardProperties = new LinkedList<String>(toCopy.forwardProperties);
            this.backwardProperties = new LinkedList<String>(toCopy.backwardProperties);
            original = false;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[\n\tbase: " + base);
            sb.append("\n\tparentSubject: " + parentSubject);
            sb.append("\n\tparentObject: " + parentObject);
            //sb.append("\nforward: [");
            //for (String prop : forwardProperties) {
            //    sb.append(prop);
            //    sb.append(" ");
            //}
            sb.append("]");
            return sb.toString();
        }
    }
}
