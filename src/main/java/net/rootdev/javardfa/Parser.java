/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * @author Damian Steer <pldms@mac.com>
 */
public class Parser implements ContentHandler {
    
    private final XMLOutputFactory outputFactory;
    private final XMLEventFactory eventFactory;
    private final XMLEventReader reader;
    private final StatementSink sink;
    private final Set<Setting> settings;
    private final Constants consts;
    private final Resolver resolver;

    private final StartElement fakeEnvelope;

    public Parser(StatementSink sink) {
        this(   sink,
                XMLOutputFactory.newInstance(),
                XMLEventFactory.newInstance(),
                new IRIResolver());
    }

    public Parser(StatementSink sink,
            XMLOutputFactory outputFactory,
            XMLEventFactory eventFactory,
            Resolver resolver) {
        this.sink = sink;
        this.outputFactory = outputFactory;
        this.eventFactory = eventFactory;
        this.reader = null;
        this.settings = EnumSet.noneOf(Setting.class);
        this.consts = new Constants();
        this.resolver = resolver;

        // Important, although I guess the caller doesn't get total control
        outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);

        // Woodstox doesn't like producing xml fragments, so we wrap.
        fakeEnvelope = eventFactory.createStartElement("", null, "fake");
    }

    public void enable(Setting setting) {
        settings.add(setting);
    }

    public void disable(Setting setting) {
        settings.remove(setting);
    }

    public void setBase(String base) {
        this.context = new EvalContext(base);
    }

    EvalContext parse(EvalContext context, StartElement element) throws XMLStreamException {
        boolean recurse = true;
        boolean skipElement = false;
        String newSubject = null;
        String currentObject = null;
        List<String> forwardProperties = new LinkedList(context.forwardProperties);
        List<String> backwardProperties = new LinkedList(context.backwardProperties);
        String currentLanguage = context.language;
        boolean langIsLang = context.langIsLang;

        if (element.getAttributeByName(consts.xmllang) != null) {
            currentLanguage = element.getAttributeByName(consts.xmllang).getValue();
        }

        if (settings.contains(Setting.ManualNamespaces) &&
                element.getAttributeByName(consts.fakeXmlLang) != null &&
                !langIsLang) {
            currentLanguage = element.getAttributeByName(consts.fakeXmlLang).getValue();
        }

        if (settings.contains(Setting.ManualNamespaces) &&
                element.getAttributeByName(consts.lang) != null) {
            langIsLang = true;
            currentLanguage = element.getAttributeByName(consts.lang).getValue();
        }

        if (consts.base.equals(element.getName()) &&
                element.getAttributeByName(consts.href) != null) {
            context.setBase(element.getAttributeByName(consts.href).getValue());
        }

        if (element.getAttributeByName(consts.rev) == null &&
                element.getAttributeByName(consts.rel) == null) {
            Attribute nSubj = findAttribute(element, consts.about, consts.src, consts.resource, consts.href);
            if (nSubj != null) {
                newSubject = getURI(context.base, element, nSubj);
            } else {
                if (element.getAttributeByName(consts.typeof) != null) {
                    if (consts.body.equals(element.getName()) ||
                            consts.head.equals(element.getName())) {
                        newSubject = context.base;
                    } else {
                        newSubject = createBNode();
                    }
                } else {
                    if (context.parentObject != null) {
                        newSubject = context.parentObject;
                    }
                    if (element.getAttributeByName(consts.property) == null) {
                        skipElement = true;
                    }
                }
            }
        } else {
            Attribute nSubj = findAttribute(element, consts.about, consts.src);
            if (nSubj != null) {
                newSubject = getURI(context.base, element, nSubj);
            } else {
                // TODO if element is head or body assume about=""
                if (element.getAttributeByName(consts.typeof) != null) {
                    newSubject = createBNode();
                } else {
                    if (context.parentObject != null) {
                        newSubject = context.parentObject;
                    }
                }
            }
            Attribute cObj = findAttribute(element, consts.resource, consts.href);
            if (cObj != null) {
                currentObject = getURI(context.base, element, cObj);
            }
        }

        if (newSubject != null && element.getAttributeByName(consts.typeof) != null) {
            List<String> types = getURIs(context.base, element, element.getAttributeByName(consts.typeof));
            for (String type : types) {
                emitTriples(newSubject,
                        consts.rdfType,
                        type);
            }
        }

        if (newSubject == null) {
            newSubject = context.parentSubject;
        }

        // Dodgy extension
        if (settings.contains(Setting.FormMode)) {
            if (consts.form.equals(element.getName())) {
                emitTriples(newSubject, consts.rdfType, "http://www.w3.org/1999/xhtml/vocab/#form"); // Signal entering form
            }
            if (consts.input.equals(element.getName()) &&
                    element.getAttributeByName(consts.name) != null) {
                currentObject = "?" + element.getAttributeByName(consts.name).getValue();
            }

        }

        if (currentObject != null) {
            if (element.getAttributeByName(consts.rel) != null) {
                emitTriples(newSubject,
                        getURIs(context.base, element, element.getAttributeByName(consts.rel)),
                        currentObject);
            }
            if (element.getAttributeByName(consts.rev) != null) {
                emitTriples(currentObject,
                        getURIs(context.base, element, element.getAttributeByName(consts.rev)),
                        newSubject);
            }
        } else {
            if (element.getAttributeByName(consts.rel) != null) {
                forwardProperties.addAll(getURIs(context.base, element, element.getAttributeByName(consts.rel)));
            }
            if (element.getAttributeByName(consts.rev) != null) {
                backwardProperties.addAll(getURIs(context.base, element, element.getAttributeByName(consts.rev)));
            }
            if (element.getAttributeByName(consts.rel) != null || // if predicate present
                    element.getAttributeByName(consts.rev) != null) {
                currentObject = createBNode();
            }
        }

        // Getting literal values. Complicated!

        if (element.getAttributeByName(consts.property) != null) {
            List<String> props = getURIs(context.base, element, element.getAttributeByName(consts.property));
            String dt = getDatatype(element);
            if (element.getAttributeByName(consts.content) != null) { // The easy bit
                String lex = element.getAttributeByName(consts.content).getValue();
                if (dt == null || dt.length() == 0) {
                    emitTriplesPlainLiteral(newSubject, props, lex, currentLanguage);
                } else {
                    emitTriplesDatatypeLiteral(newSubject, props, lex, dt);
                }
            } else {
                //recurse = false;
                level = 1;
                theDatatype = dt;
                literalWriter = new StringWriter();
                litProps = props;
                if (dt == null) // either plain or xml. defer decision
                {
                    queuedEvents = new LinkedList<XMLEvent>();
                } else if (consts.xmlLiteral.equals(dt)) // definitely xml?
                {
                    XMLStreamWriter xsw = outputFactory.createXMLStreamWriter(literalWriter);
                    //xmlWriter = outputFactory.createXMLEventWriter(literalWriter);
                    xmlWriter = new CanonicalXMLEventWriter(xsw);
                    xmlWriter.add(fakeEnvelope);
                }

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
                ec.langIsLang = langIsLang;
                ec.original = context.original;
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

                ec.language = currentLanguage;
                ec.langIsLang = langIsLang;
                ec.forwardProperties = forwardProperties;
                ec.backwardProperties = backwardProperties;
            }

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
    int bnodeId = 0;

    private String createBNode() // TODO probably broken? Can you write bnodes in rdfa directly?
    {
        return "_:node" + (bnodeId++);
    }

    private String getDatatype(StartElement element) {
        Attribute de = element.getAttributeByName(consts.datatype);
        if (de == null) {
            return null;
        }
        String dt = de.getValue();
        if (dt.length() == 0) {
            return dt;
        }
        return expandCURIE(element, dt);
    }

    private void getNamespaces(Attributes attrs) {
        for (int i = 0; i < attrs.getLength(); i++) {
            String qname = attrs.getQName(i);
            String prefix = getPrefix(qname);
            if ("xmlns".equals(prefix)) {
                String pre = getLocal(prefix, qname);
                String uri = attrs.getValue(i);
                context.setNamespaceURI(pre, uri);
                sink.addPrefix(pre, uri);
            }
        }
    }

    private String getPrefix(String qname) {
        if (!qname.contains(":")) {
            return "";
        }
        return qname.substring(0, qname.indexOf(":"));
    }

    private String getLocal(String prefix, String qname) {
        if (prefix.length() == 0) {
            return qname;
        }
        return qname.substring(prefix.length() + 1);
    }
    /**
     * SAX methods
     */
    private Locator locator;
    //private NSMapping mapping;
    private EvalContext context; // = new EvalContext("BASE_NOT_SET");
    // For literals (what fun!)
    private List<XMLEvent> queuedEvents;
    private int level = -1;
    private XMLEventWriter xmlWriter;
    private StringWriter literalWriter;
    private String theDatatype;
    private List<String> litProps;

    public void setDocumentLocator(Locator arg0) {
        this.locator = arg0;
        if (locator.getSystemId() != null)
            this.setBase(arg0.getSystemId());
    }

    public void startDocument() throws SAXException {
        sink.start();
    }

    public void endDocument() throws SAXException {
        sink.end();
    }

    public void startPrefixMapping(String arg0, String arg1)
            throws SAXException {
        context.setNamespaceURI(arg0, arg1);
        sink.addPrefix(arg0, arg1);
    }

    public void endPrefixMapping(String arg0) throws SAXException {
    }

    public void startElement(String arg0, String localname, String qname, Attributes arg3) throws SAXException {
        try {
            //System.err.println("Start element: " + arg0 + " " + arg1 + " " + arg2);

            // This is set very late in some html5 cases (not even ready by document start)
            if (context == null) {
                this.setBase(locator.getSystemId());
            }
            // Dammit, not quite the same as XMLEventFactory
            String prefix = (localname.equals(qname)) ? ""
                    : qname.substring(0, qname.indexOf(':'));
            if (settings.contains(Setting.ManualNamespaces)) {
                getNamespaces(arg3);
            }
            StartElement e = eventFactory.createStartElement(
                    prefix, arg0, localname,
                    fromAttributes(arg3), null, context);

            if (level != -1) { // getting literal
                handleForLiteral(e);
                return;
            }
            context = parse(context, e);
        } catch (XMLStreamException ex) {
            throw new RuntimeException("Streaming issue", ex);
        }

    }

    public void endElement(String arg0, String localname, String qname) throws SAXException {
        //System.err.println("End element: " + arg0 + " " + arg1 + " " + arg2);
        if (level != -1) { // getting literal
            String prefix = (localname.equals(qname)) ? ""
                    : qname.substring(0, qname.indexOf(':'));
            XMLEvent e = eventFactory.createEndElement(prefix, arg0, localname);
            handleForLiteral(e);
            if (level != -1) {
                return; // if still handling literal duck out now
            }
        }
        context = context.parent;
    }

    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        if (level != -1) {
            XMLEvent e = eventFactory.createCharacters(String.valueOf(arg0, arg1, arg2));
            handleForLiteral(e);
            return;
        }
    }

    public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
        //System.err.println("Whitespace...");
        if (level != -1) {
            XMLEvent e = eventFactory.createIgnorableSpace(String.valueOf(arg0, arg1, arg2));
            handleForLiteral(e);
        }
    }

    public void processingInstruction(String arg0, String arg1) throws SAXException {
    }

    public void skippedEntity(String arg0) throws SAXException {
    }

    private Iterator fromAttributes(Attributes attributes) {
        List toReturn = new LinkedList();
        boolean haveLang = false;
        for (int i = 0; i < attributes.getLength(); i++) {
            String qname = attributes.getQName(i);
            String prefix = qname.contains(":") ? qname.substring(0, qname.indexOf(":")) : "";
            Attribute attr = eventFactory.createAttribute(
                    prefix, attributes.getURI(i),
                    attributes.getLocalName(i), attributes.getValue(i));
            if (consts.xmllang.getLocalPart().equals(attributes.getLocalName(i)) &&
                    consts.xmllang.getNamespaceURI().equals(attributes.getURI(i))) {
                haveLang = true;
            }
            toReturn.add(attr);
        }
        // Copy xml lang across if in literal
        if (level == 1 && context.language != null && !haveLang) {
            toReturn.add(eventFactory.createAttribute(consts.xmllang, context.language));
        }
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
                XMLStreamWriter xsw = outputFactory.createXMLStreamWriter(literalWriter);
                //xmlWriter = outputFactory.createXMLEventWriter(literalWriter);
                xmlWriter = new CanonicalXMLEventWriter(xsw);
                xmlWriter.add(fakeEnvelope);
                for (XMLEvent ev : queuedEvents) {
                    xmlWriter.add(ev);
                }
                queuedEvents = null;
                theDatatype = consts.xmlLiteral;
            }
        }

        if (e.isEndElement()) {
            level--;
            if (level == 0) { // Finished!
                if (xmlWriter != null) {
                    xmlWriter.close();
                } else if (queuedEvents != null) {
                    for (XMLEvent ev : queuedEvents) {
                        literalWriter.append(ev.asCharacters().getData());
                    }
                }
                String lex = literalWriter.toString();
                if (consts.xmlLiteral.equals(theDatatype)) {
                    // Clean up fake doc root
                    if (lex.startsWith("<fake>"))  lex = lex.substring(6);
                    else if (lex.startsWith("<fake xmlns=\"\">")) lex = lex.substring(15);
                    if (lex.endsWith("</fake>")) lex = lex.substring(0, lex.length() - 7);
                }
                if (theDatatype == null || theDatatype.length() == 0) {
                    emitTriplesPlainLiteral(context.parentSubject,
                            litProps, lex, context.language);
                } else {
                    emitTriplesDatatypeLiteral(context.parentSubject,
                            litProps, lex, theDatatype);
                }
                queuedEvents = null;
                xmlWriter = null;
                literalWriter = null;
                theDatatype = null;
                litProps = null;
                level = -1;
                return;
            }
        }

        if (xmlWriter != null) {
            xmlWriter.add(e);
        } else if (e.isCharacters() && queuedEvents != null) {
            queuedEvents.add(e);
        } else if (e.isCharacters()) {
            literalWriter.append(e.asCharacters().getData());
        }
    }

    public String getURI(String base, StartElement element, Attribute attr) {
        QName attrName = attr.getName();
        if (attrName.equals(consts.href) || attrName.equals(consts.src)) // A URI
        {
            if (attr.getValue().length() == 0) return base;
            else return resolver.resolve(base, attr.getValue());
        }
        if (attrName.equals(consts.about) || attrName.equals(consts.resource)) // Safe CURIE or URI
        {
            return expandSafeCURIE(base, element, attr.getValue());
        }
        if (attrName.equals(consts.datatype)) // A CURIE
        {
            return expandCURIE(element, attr.getValue());
        }
        throw new RuntimeException("Unexpected attribute: " + attr);
    }

    public List<String> getURIs(String base, StartElement element, Attribute attr) {
        List<String> uris = new LinkedList<String>();
        String[] curies = attr.getValue().split("\\s+");
        boolean permitReserved = consts.rel.equals(attr.getName()) ||
                consts.rev.equals(attr.getName());
        for (String curie : curies) {
            boolean isSpecial = (settings.contains(Setting.ManualNamespaces)) ? consts.SpecialRels.contains(curie.toLowerCase()) : consts.SpecialRels.contains(curie);
            if (isSpecial && settings.contains(Setting.ManualNamespaces)) {
                curie = curie.toLowerCase();
            }
            if (permitReserved && isSpecial) {
                uris.add("http://www.w3.org/1999/xhtml/vocab#" + curie);
            } else if (!isSpecial) {
                String uri = expandCURIE(element, curie);
                if (uri != null) {
                    uris.add(uri);
                }
            }
        }
        return uris;
    }

    public String expandCURIE(StartElement element, String value) {
        if (value.startsWith("_:") && element.getNamespaceURI("_") == null) {
            return value;
        }
        if (settings.contains(Setting.FormMode) && // variable
                value.startsWith("?")) {
            return value;
        }
        int offset = value.indexOf(":") + 1;
        if (offset == 0) {
            //throw new RuntimeException("Is this a curie? \"" + value + "\"");
            return null;
        }
        String prefix = value.substring(0, offset - 1);
        String namespaceURI = prefix.length() == 0 ? "http://www.w3.org/1999/xhtml/vocab#" : element.getNamespaceURI(prefix);
        if (namespaceURI == null) {
            return null;
            //throw new RuntimeException("Unknown prefix: " + prefix);
        }
        if (offset != value.length() && value.charAt(offset) == '#') {
            offset += 1; // ex:#bar
        }
        if (namespaceURI.endsWith("/") || namespaceURI.endsWith("#")) {
            return namespaceURI + value.substring(offset);
        } else {
            return namespaceURI + "#" + value.substring(offset);
        }
    }

    public String expandSafeCURIE(String base, StartElement element, String value) {
        if (value.startsWith("[") && value.endsWith("]")) {
            return expandCURIE(element, value.substring(1, value.length() - 1));
        } else {
            if (value.length() == 0) {
                return base;
            }

            if (settings.contains(Setting.FormMode) &&
                    value.startsWith("?")) {
                return value;
            }

            return resolver.resolve(base, value);
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
