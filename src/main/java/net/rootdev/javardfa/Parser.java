/*
 * (c) 2009
 * Damian Steer <mailto:pldms@mac.com>
 */
package net.rootdev.javardfa;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 *
 * @author pldms
 */
public class Parser implements ContentHandler {

    final static List<String> _allowed = Arrays.asList(
            "alternate", "appendix", "bookmark", "cite",
            "chapter", "contents", "copyright", "first",
            "glossary", "help", "icon", "index", "last",
            "license", "meta", "next", "p3pv1", "prev",
            "collection", "role", "section", "stylesheet",
            "subsection", "start", "top", "up" );

    final static Set<String> SpecialRels = new HashSet<String>(_allowed);

    final static IRIFactory IRIFact = IRIFactory.semanticWebImplementation();
    final static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    final static XMLEventFactory EventFactory = XMLEventFactory.newInstance();

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
    final QName lang = new QName("http://www.w3.org/XML/1998/namespace", "lang", "xml");
    final QName base = new QName("http://www.w3.org/1999/xhtml", "base");
    final QName head = new QName("http://www.w3.org/1999/xhtml", "head");
    final QName body = new QName("http://www.w3.org/1999/xhtml", "body");
    // Hack bits
    final QName input = new QName("input");
    final QName name = new QName("name");
    final QName form = new QName("form");

    final Collection<String> rdfType = Collections.singleton("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    final String xmlLiteral = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";

    final Set<Setting> settings = EnumSet.noneOf(Setting.class);

    public enum Setting {
        FormMode, ManualNamespaces
    }

    public Parser(StatementSink sink) {
        this.reader = null;
        this.sink = sink;
        outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
    }

    public void enable(Setting setting) { settings.add(setting); }

    public void disable(Setting setting) { settings.remove(setting); }

    public void setBase(String base) {
        this.context = new EvalContext(base);
    }

    //private String currentBase;

    EvalContext parse(EvalContext context, StartElement element)
            throws XMLStreamException, IOException {
        boolean recurse = true;
        boolean skipElement = false;
        String newSubject = null;
        String currentObject = null;
        //Map<String, String> uriMappings = context.uriMappings;
        List<String> forwardProperties = new LinkedList(context.forwardProperties);
        List<String> backwardProperties = new LinkedList(context.backwardProperties);
        String currentLanguage = context.language;

        // TODO element.getNamespace();

        if (element.getAttributeByName(lang) != null)
            currentLanguage = element.getAttributeByName(lang).getValue();

        if (base.equals(element.getName()) && element.getAttributeByName(href) != null) {
            context.setBase(element.getAttributeByName(href).getValue());
        }

        if (element.getAttributeByName(rev) == null &&
                element.getAttributeByName(rel) == null) {
            Attribute nSubj = findAttribute(element, about, src, resource, href);
            if (nSubj != null) {
                newSubject = getURI(context.base, element, nSubj);
            } else {
                if (element.getAttributeByName(typeof) != null) {
                    if (body.equals(element.getName()) ||
                            head.equals(element.getName()))
                        newSubject = context.base;
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
                newSubject = getURI(context.base, element, nSubj);
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
                currentObject = getURI(context.base, element, cObj);
            }
        }

        if (newSubject != null && element.getAttributeByName(typeof) != null) {
            List<String> types = getURIs(context.base, element, element.getAttributeByName(typeof));
            for (String type : types) {
                emitTriples(newSubject,
                        rdfType,
                        type);
            }
        }

        if (newSubject == null) newSubject = context.parentSubject;

        // Dodgy extension
        if (settings.contains(Setting.FormMode)) {
            if (form.equals(element.getName()))
                emitTriples(newSubject, rdfType, "http://www.w3.org/1999/xhtml/vocab/#form"); // Signal entering form
            if (input.equals(element.getName()) &&
                    element.getAttributeByName(name) != null)
                currentObject = "?:" + element.getAttributeByName(name).getValue();

        }

        if (currentObject != null) {
            if (element.getAttributeByName(rel) != null) {
                emitTriples(newSubject,
                        getURIs(context.base, element, element.getAttributeByName(rel)),
                        currentObject);
            }
            if (element.getAttributeByName(rev) != null) {
                emitTriples(currentObject,
                        getURIs(context.base, element, element.getAttributeByName(rev)),
                        newSubject);
            }
        } else {
            if (element.getAttributeByName(rel) != null) {
                forwardProperties.addAll(getURIs(context.base, element, element.getAttributeByName(rel)));
            }
            if (element.getAttributeByName(rev) != null) {
                backwardProperties.addAll(getURIs(context.base, element, element.getAttributeByName(rev)));
            }
            if (element.getAttributeByName(rel) != null || // if predicate present
                    element.getAttributeByName(rev) != null) {
                currentObject = createBNode(); // TODO generate bnode
            }
        }

        // Getting literal values. Complicated!
        
        if (element.getAttributeByName(property) != null) {
            List<String> props = getURIs(context.base, element, element.getAttributeByName(property));
            String dt = getDatatype(element);
            if (element.getAttributeByName(content) != null) { // The easy bit
                String lex = element.getAttributeByName(content).getValue();
                if (dt == null || dt.length() == 0)
                    emitTriplesPlainLiteral(newSubject, props, lex, currentLanguage);
                else
                    emitTriplesDatatypeLiteral(newSubject, props, lex, dt);
            } else {
                //recurse = false;
                level = 1;
                theDatatype = dt;
                literalWriter = new StringWriter();
                litProps = props;
                if (dt == null) // either plain or xml
                    queuedEvents = new LinkedList<XMLEvent>();
                else if (dt.length() == 0) // force plain
                    ;
                else if (xmlLiteral.equals(dt)) // definitely xml?
                    xmlWriter = outputFactory.createXMLEventWriter(literalWriter);

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
            /*
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {
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
             */

            return ec;
        }

        return null;
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

    private String getURI(String base, StartElement element, Attribute attr) {
        QName attrName = attr.getName();
        if (attrName.equals(href) || attrName.equals(src)) // A URI
        {
            if (attr.getValue().length() == 0) return base;
            IRI uri = IRIFact.construct(base);
            IRI resolved = uri.resolve(attr.getValue());
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
        boolean permitReserved = rel.equals(attr.getName()) ||
                    rev.equals(attr.getName());
        for (String curie : curies) {
            if (permitReserved && SpecialRels.contains(curie))
                uris.add("http://www.w3.org/1999/xhtml/vocab#" + curie);
            else if (!SpecialRels.contains(curie)) {
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
        if (settings.contains(Setting.FormMode) && // variable
                value.startsWith("?:")) return value;
        int offset = value.indexOf(":");
        if (offset == -1) {
            //throw new RuntimeException("Is this a curie? \"" + value + "\"");
            return null;
        }
        String prefix = value.substring(0, offset);
        String namespaceURI = prefix.length() == 0 ?
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

    private String expandSafeCURIE(String base, StartElement element, String value) {
        if (value.startsWith("[") && value.endsWith("]")) {
            return expandCURIE(element, value.substring(1, value.length() - 1));
        } else {
            if (value.length() == 0) return base;

            if (settings.contains(Setting.FormMode) &&
                    value.startsWith("?:")) return value;

            IRI uri = IRIFact.construct(base);
            IRI resolved = uri.resolve(value);
            return resolved.toString();
        }
    }

    private String getDatatype(StartElement element) {
        Attribute de = element.getAttributeByName(datatype);
        if (de == null) {
            return null;
        }
        String dt = de.getValue();
        if (dt.length() == 0) {
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

        EvalContext parent;
        String base;
        String parentSubject;
        String parentObject;
        String language;
        List<String> forwardProperties;
        List<String> backwardProperties;
        boolean original;

        private EvalContext(String base) {
            this.base = base;
            this.parentSubject = base;
            this.forwardProperties = new LinkedList<String>();
            this.backwardProperties = new LinkedList<String>();
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
            this.parent = toCopy;
        }

        public void setBase(String abase) {
            if (abase.contains("#"))
                this.base = abase.substring(0, abase.indexOf("#"));
            else
                this.base = abase;
            if (this.original) this.parentSubject = this.base;
            if (parent != null) parent.setBase(base);
        }



        //@Override
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

    static class Appender implements Iterator {

        final Iterator parent;
        final Object appended;
        boolean finished;

        public Appender(Iterator parent, Object appended) {
            this.parent = parent;
            this.appended = appended;
            finished = false;
        }

        //@Override
        public boolean hasNext() {
            return parent.hasNext() || !finished;
        }

        //@Override
        public Object next() {
            if (parent.hasNext()) return parent.next();
            if (finished) throw new NoSuchElementException("I'm empty, dum dum");
            finished = true;
            return appended;
        }

        //@Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported");
        }

    }

    /**
     * SAX methods
     */

    private Locator locator;
    private NSMapping mapping;
    private EvalContext context = new EvalContext("http://www.example.com/");
    
    // For literals (what fun!)
    private List<XMLEvent> queuedEvents;
    private int level = -1;
    private XMLEventWriter xmlWriter;
    private StringWriter literalWriter;
    private String theDatatype;
    private List<String> litProps;

    public void setDocumentLocator(Locator arg0) { this.locator = arg0; }

    public void startDocument() throws SAXException {
        sink.start();
        mapping = new NSMapping();
    }

    public void endDocument() throws SAXException { sink.end(); }

    public void startPrefixMapping(String arg0, String arg1) throws SAXException {
        //System.err.println("Mapping: " + arg0 + " " + arg1);
        mapping.add(arg0, arg1);
    }

    public void endPrefixMapping(String arg0) throws SAXException {
        mapping.remove(arg0);
    }

    public void startElement(String arg0, String localname, String qname, Attributes arg3) throws SAXException {
        try {
            //System.err.println("Start element: " + arg0 + " " + arg1 + " " + arg2);
            // Dammit, not quit the same as XMLEventFactory
            String prefix = (localname.equals(qname)) ? ""
                    : qname.substring(0, qname.indexOf(':'));
            StartElement e = EventFactory.createStartElement(
                    prefix, arg0, localname,
                    fromAttributes(arg3), mapping.current(), mapping);

            if (level != -1) { // getting literal
                handleForLiteral(e);
                return;
            }
            //System.err.println("Start: " + qname);
            //System.err.println("Context is : " + context.hashCode());
            context = parse(context, e);
            //System.err.println("Context now: " + context.hashCode());
        } catch (XMLStreamException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void endElement(String arg0, String localname, String qname) throws SAXException {
        //System.err.println("End element: " + arg0 + " " + arg1 + " " + arg2);
        if (level != -1) { // getting literal
            String prefix = (localname.equals(qname)) ? ""
                    : qname.substring(0, qname.indexOf(':'));
            XMLEvent e = EventFactory.createEndElement(prefix, arg0, localname);
            handleForLiteral(e);
            if (level != -1) return; // if still handling literal duck out now
        }
        //System.err.println("End: " + qname);
        //System.err.println("Context is : " + context.hashCode());
        context = context.parent;
        //System.err.println("Context now: " + context.hashCode());
    }

    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        if (level != -1) {
            XMLEvent e = EventFactory.createCharacters(String.valueOf(arg0, arg1, arg2));
            handleForLiteral(e);
            return;
        }
    }

    public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
        //System.err.println("Whitespace...");
        if (level != -1) {
            XMLEvent e = EventFactory.createIgnorableSpace(String.valueOf(arg0, arg1, arg2));
            handleForLiteral(e);
        }
    }

    public void processingInstruction(String arg0, String arg1) throws SAXException {}

    public void skippedEntity(String arg0) throws SAXException {}

    private Iterator fromAttributes(Attributes attributes) {
        List toReturn = new LinkedList();
        boolean haveLang = false;
        for (int i = 0; i < attributes.getLength(); i++) {
            String qname = attributes.getQName(i);
            String prefix = qname.contains(":") ?
                qname.substring(0, qname.indexOf(":")) : "";
            Attribute attr = EventFactory.createAttribute(
                    prefix, attributes.getURI(i),
                    attributes.getLocalName(i), attributes.getValue(i));
            if (lang.getLocalPart().equals(attributes.getLocalName(i)) &&
                    lang.getNamespaceURI().equals(attributes.getURI(i)))
                haveLang = true;
            toReturn.add(attr);
        }
        // Copy xml lang across if in literal
        if (level == 1 && context.language != null && !haveLang)
            toReturn.add(EventFactory.createAttribute(lang, context.language));
        return toReturn.iterator();
    }

    private void handleForLiteral(XMLEvent e) {
        try {
            handleForLiteralEx(e);
        } catch (XMLStreamException ex) {
            throw new RuntimeException("Literal handling error", ex);
        } catch (IOException ex) {
            throw new RuntimeException("Literal handling error", ex);
        }
    }

    private void handleForLiteralEx(XMLEvent e) throws XMLStreamException, IOException {
        if (e.isStartElement()) {
            level++;
            if (queuedEvents != null) { // Aha, we ain't plain
                xmlWriter = outputFactory.createXMLEventWriter(literalWriter);
                for (XMLEvent ev: queuedEvents) xmlWriter.add(ev);
                queuedEvents = null;
                theDatatype = xmlLiteral;
            }
        }

        if (e.isEndElement()) {
            level--;
            if (level == 0) { // Finished!
                if (xmlWriter != null) xmlWriter.close();
                else if (queuedEvents != null) {
                    for (XMLEvent ev: queuedEvents)
                        literalWriter.append(ev.asCharacters().getData());
                }
                String lex = literalWriter.toString();
                if (theDatatype == null || theDatatype.length() == 0)
                    emitTriplesPlainLiteral(context.parentSubject,
                            litProps, lex, context.language);
                else
                    emitTriplesDatatypeLiteral(context.parentSubject,
                            litProps, lex, theDatatype);
                queuedEvents = null;
                xmlWriter = null;
                literalWriter = null;
                theDatatype = null;
                litProps = null;
                level = -1;
                return;
            }
        }

        if (xmlWriter != null) xmlWriter.add(e);
        else if (e.isCharacters() && queuedEvents != null)
            queuedEvents.add(e);
        else if (e.isCharacters())
            literalWriter.append(e.asCharacters().getData());
    }

    static class NSMapping implements NamespaceContext {
        Map<String, LinkedList<String>> mappings =
                new HashMap<String, LinkedList<String>>();

        public String getNamespaceURI(String prefix) {
            if (mappings.containsKey(prefix))
                return mappings.get(prefix).getLast();
            return null;
        }

        public void add(String prefix, String uri) {
            if (mappings.containsKey(prefix))
                mappings.get(prefix).add(uri);
            else {
                LinkedList<String> list = new LinkedList<String>();
                list.add(uri);
                mappings.put(prefix, list);
            }
        }

        public void remove(String prefix) {
            if (mappings.containsKey(prefix)) {
                mappings.get(prefix).removeLast();
                if (mappings.get(prefix).isEmpty())
                    mappings.remove(prefix);
            }
        }

        // This is all wrong. current just means defined on element, I think
        public Iterator<Namespace> current() {
            List<Namespace> toReturn = new LinkedList<Namespace>();
            for (Entry<String, LinkedList<String>> e: mappings.entrySet()) {
                toReturn.add(
                        EventFactory.createNamespace(e.getKey(), e.getValue().getLast())
                        );
            }
            //return toReturn.iterator();
            return null;
        }

        public String getPrefix(String arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Iterator getPrefixes(String arg0) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
