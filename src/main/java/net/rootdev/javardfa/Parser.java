/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa;

import net.rootdev.javardfa.uri.URIExtractor10;
import net.rootdev.javardfa.uri.URIExtractor;
import net.rootdev.javardfa.uri.IRIResolver;
import net.rootdev.javardfa.literal.LiteralCollector;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Damian Steer <pldms@mac.com>
 */
public class Parser implements ContentHandler, ErrorHandler {

    private final XMLEventFactory eventFactory;
    private final StatementSink sink;
    private final Set<Setting> settings;
    private final LiteralCollector literalCollector;
    private final URIExtractor extractor;

    public Parser(StatementSink sink) {
        this(   sink,
                XMLOutputFactory.newInstance(),
                XMLEventFactory.newInstance(),
                new URIExtractor10(new IRIResolver()));
    }

    public Parser(StatementSink sink,
            XMLOutputFactory outputFactory,
            XMLEventFactory eventFactory,
            URIExtractor extractor) {
        this.sink = sink;
        this.eventFactory = eventFactory;
        this.settings = EnumSet.noneOf(Setting.class);
        this.extractor = extractor;
        this.literalCollector = new LiteralCollector(this, eventFactory, outputFactory);

        extractor.setSettings(settings);

        // Important, although I guess the caller doesn't get total control
        outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
    }
    
    public boolean isEnabled(Setting setting) {
        return settings.contains(setting);
    }
    
    public void enable(Setting setting) {
        settings.add(setting);
    }

    public void disable(Setting setting) {
        settings.remove(setting);
    }
    
    public void setBase(String base) {
        this.context = new EvalContext(base);
        if (isEnabled(Setting.OnePointOne)) context.setPrefixes(Constants.CORE_DEFAULT_PREFIXES);
        sink.setBase(context.getBase());
    }

    EvalContext parse(EvalContext context, StartElement element)
            throws XMLStreamException {
        String currentLanguage = context.language;
        boolean inXHTML = Constants.xhtmlNS.equals(element.getName().getNamespaceURI());
        
        // Respect xml:base outside xhtml
        if (element.getAttributeByName(Constants.xmlbaseNS) != null && !inXHTML) {
            context.setBase(element.getAttributeByName(Constants.xmlbaseNS).getValue());
            sink.setBase(context.getBase());
        }
        
        if (Constants.base.equals(element.getName()) &&
                element.getAttributeByName(Constants.href) != null) {
            context.setBase(element.getAttributeByName(Constants.href).getValue());
            sink.setBase(context.getBase());
        }
        
        // The xml / html namespace matching is a bit ropey. I wonder if the html 5
        // parser has a setting for this?
        if (settings.contains(Setting.ManualNamespaces)) {
            if (element.getAttributeByName(Constants.xmllang) != null) {
                currentLanguage = element.getAttributeByName(Constants.xmllang).getValue();
                if (currentLanguage.length() == 0) currentLanguage = null;
            } else if (element.getAttributeByName(Constants.lang) != null) {
                currentLanguage = element.getAttributeByName(Constants.lang).getValue();
                if (currentLanguage.length() == 0) currentLanguage = null;
            }
        } else if (element.getAttributeByName(Constants.xmllangNS) != null) {
            currentLanguage = element.getAttributeByName(Constants.xmllangNS).getValue();
            if (currentLanguage.length() == 0) currentLanguage = null;
        }
        
        if (settings.contains(Setting.OnePointOne)) {

            if (element.getAttributeByName(Constants.vocab) != null) {
                String vocab =
                    element.getAttributeByName(Constants.vocab).getValue().trim();
                // empty vocab removes default vocab
                if (vocab.length() == 0) {
                    context.vocab = null;
                } else {
                    context.vocab = vocab;
                    emitTriples(context.base, Constants.rdfaUses, vocab);
                }
            }

            if (element.getAttributeByName(Constants.prefix) != null) {
                parsePrefixes(element.getAttributeByName(Constants.prefix).getValue(), context);
            }
        }
        
        String about = extractor.getURI(element, Constants.about, context);
        String src = extractor.getURI(element, Constants.src, context);
        String href = extractor.getURI(element, Constants.href, context);
        String resource = extractor.getURI(element, Constants.resource, context);
        String datatype = extractor.getURI(element, Constants.datatype, context);
        Attribute contentAttr = element.getAttributeByName(Constants.content);
        String content = (contentAttr == null) ? null : contentAttr.getValue();
        
        List<String> typeof = extractor.getURIs(element, Constants.typeof, context);
        List<String> rel = extractor.getURIs(element, Constants.rel, context);
        List<String> rev = extractor.getURIs(element, Constants.rev, context);
        List<String> property = extractor.getURIs(element, Constants.property, context);
        
        if (settings.contains(Setting.OnePointOne)) {
            return parse11(rev, rel, about, src, resource, href, context, inXHTML, 
                    element, typeof, property, content, datatype, currentLanguage);
        } else {
            return parse10(rev, rel, about, src, resource, href, context, inXHTML,
                    element, typeof, property, content, datatype, currentLanguage);
        }
    }

    private EvalContext parse10(List<String> rev, List<String> rel, String about, String src, String resource, String href, EvalContext context, boolean inXHTML, StartElement element, List<String> typeof, List<String> property, String content, String datatype, String currentLanguage) {
        boolean skipElement = false;
        String newSubject = null;
        String currentObject = null;
        List<String> forwardProperties = new LinkedList();
        List<String> backwardProperties = new LinkedList();
        
        if (rev == null && rel == null) {
            newSubject = coalesce(about, src, resource, href);
            if (newSubject == null) {
                if (context.parent == null && !inXHTML) {
                    newSubject = context.base;
                } else if (Constants.body.equals(element.getName()) ||
                            Constants.head.equals(element.getName())) {
                    newSubject = context.base;
                } else if (typeof != null) {
                    newSubject = createBNode();
                } else {
                    if (context.parentObject != null) {
                        newSubject = context.parentObject;
                    }
                    if (property == null) {
                        skipElement = true;
                    }
                }
            }
        } else {
            newSubject = coalesce(about, src);
            if (newSubject == null) {
                if (context.parent == null && !inXHTML) {
                    newSubject = context.base;
                } else if (Constants.head.equals(element.getName()) ||
                        Constants.body.equals(element.getName())) {
                    newSubject = context.base;
                } else if (typeof != null) {
                    newSubject = createBNode();
                } else if (context.parentObject != null) {
                    newSubject = context.parentObject;
                }
            }
            currentObject = coalesce(resource, href);
        }

        if (newSubject != null && typeof != null) {
            for (String type : typeof) {
                emitTriples(newSubject,
                        Constants.rdfType,
                        type);
            }
        }

        // Dodgy extension
        if (settings.contains(Setting.FormMode)) {
            if (Constants.form.equals(element.getName())) {
                emitTriples(newSubject, Constants.rdfType, "http://www.w3.org/1999/xhtml/vocab/#form"); // Signal entering form
            }
            if (Constants.input.equals(element.getName()) &&
                    element.getAttributeByName(Constants.name) != null) {
                currentObject = "?" + element.getAttributeByName(Constants.name).getValue();
            }

        }
        
        if (property != null) {
            
            if (content != null) { // The easy bit
                if (datatype == null || datatype.length() == 0) {
                    emitTriplesPlainLiteral(newSubject, property, content, currentLanguage);
                } else {
                    emitTriplesDatatypeLiteral(newSubject, property, content, datatype);
                }
            } else {
                literalCollector.collect(newSubject, property, datatype, currentLanguage);
            }
        }
        
        if (currentObject != null) {
            if (element.getAttributeByName(Constants.rel) != null) {
                emitTriples(newSubject, rel, currentObject);
            }
            if (element.getAttributeByName(Constants.rev) != null) {
                emitTriples(currentObject, rev, newSubject);
            }
        } else {
            if (element.getAttributeByName(Constants.rel) != null) {
                forwardProperties.addAll(rel);
            }
            if (element.getAttributeByName(Constants.rev) != null) {
                backwardProperties.addAll(rev);
            }
            if (!forwardProperties.isEmpty() || !backwardProperties.isEmpty()) {
                // if predicate present
                currentObject = createBNode();
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

        EvalContext ec = new EvalContext(context);
        if (skipElement) {
            ec.language = currentLanguage;
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
            ec.forwardProperties = forwardProperties;
            ec.backwardProperties = backwardProperties;
        }
        return ec;
    }

    private EvalContext parse11(List<String> rev, List<String> rel, String about, String src, String resource, String href, EvalContext context, boolean inXHTML, StartElement element, List<String> typeof, List<String> property, String content, String datatype, String currentLanguage) {
        boolean skipElement = false;
        String newSubject = null;
        String currentObject = null;
        String typedResource = null;
        List<String> forwardProperties = new LinkedList();
        List<String> backwardProperties = new LinkedList();
        
        if (rev == null && rel == null) {
            if (property != null && content == null && datatype == null) {
                if (about != null && about != URIExtractor.NONE) newSubject = about;
                else if (context.parent == null) newSubject = context.base;
                else if (context.parentObject != null) newSubject = context.parentObject;
                
                if (typeof != null) {
                    if (about != null && about != URIExtractor.NONE) typedResource = about;
                    else if (context.parent == null) typedResource = context.base;
                    else typedResource = coalesce(resource, href, src);
                    
                    if (typedResource == null) typedResource = createBNode();
                                        
                    currentObject = typedResource;
                }
            } else {
                newSubject = coalesce(about, resource, href, src);
                                
                if (newSubject == null) {
                    if (context.parent == null) newSubject = context.base;
                    else if (typeof != null) newSubject = createBNode();
                    else if (context.parentObject != null) {
                        newSubject = context.parentObject;
                        if (property == null) skipElement = true;
                    }
                }
                
                if (typeof != null) typedResource = newSubject;
            }
        } else { // rev or rel present
            if (about != null && about != URIExtractor.NONE) newSubject = about;
            if (typeof != null) typedResource = newSubject;
            
            if (newSubject == null) {
                if (context.parent == null) newSubject = context.base;
                else if (context.parentObject != null) newSubject = context.parentObject;
            }
            
            currentObject = coalesce(resource, href, src);
            
            if (currentObject == null && typeof != null && about == null) currentObject = createBNode();
            
            if (typeof != null && about == null) typedResource = currentObject;
        }
        
        if (typedResource != null) {
            for (String type : typeof) {
                emitTriples(typedResource,
                        Constants.rdfType,
                        type);
            }
        }
        
        // STEP 8 skipped... list etc
        
        if (currentObject != null) {
            if (rel != null) emitTriples(newSubject, rel, currentObject);
            if (rev != null) emitTriples(currentObject, rev, newSubject);
        } else {
            // Do I really want to add all here, or simply assign???
            if (rel != null) forwardProperties.addAll(rel);
            if (rev != null) backwardProperties.addAll(rev);
            if (rev != null || rel != null) currentObject = createBNode();
        }
        
        if (property != null) {
                        
            String propertyValue = null;
            
            if (content != null) { // The easy bit
                if (datatype == null || datatype.length() == 0) {
                    emitTriplesPlainLiteral(newSubject, property, content, currentLanguage);
                } else {
                    emitTriplesDatatypeLiteral(newSubject, property, content, datatype);
                }
                propertyValue = URIExtractor.NONE;
            } else if (datatype != null) {
                literalCollector.collect(newSubject, property, datatype, currentLanguage);
                propertyValue = URIExtractor.NONE;
            } else if (rev == null && rev == null && content == null) {
                propertyValue = coalesce(resource, href, src);
            }
                        
            if (propertyValue == null && typeof != null && about == null) {
                propertyValue = typedResource;
            }
            
            if (propertyValue == null && content == null && datatype == null) {
                literalCollector.collect(newSubject, property, datatype, currentLanguage);
            }
            
            if (propertyValue != null && propertyValue != URIExtractor.NONE) emitTriples(newSubject, property, propertyValue);
        }
        
        if (!skipElement && newSubject != null) {
            emitTriples(context.parentSubject,
                    context.forwardProperties,
                    newSubject);

            emitTriples(newSubject,
                    context.backwardProperties,
                    context.parentSubject);
        }
        
        EvalContext ec = new EvalContext(context);
        if (skipElement) {
            ec.language = currentLanguage;
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
            ec.forwardProperties = forwardProperties;
            ec.backwardProperties = backwardProperties;
        }
        return ec;
    }

    public void emitTriples(String subj, Collection<String> props, String obj) {
        for (String prop : props) {
            if (!prop.startsWith("_")) sink.addObject(subj, prop, obj);
        }
    }

    public void emitTriplesPlainLiteral(String subj, Collection<String> props, String lex, String language) {
        for (String prop : props) {
            if (!prop.startsWith("_")) sink.addLiteral(subj, prop, lex, language, null);
        }
    }

    public void emitTriplesDatatypeLiteral(String subj, Collection<String> props, String lex, String datatype) {
        for (String prop : props) {
            if (!prop.startsWith("_")) sink.addLiteral(subj, prop, lex, null, datatype);
        }
    }

    int bnodeId = 0;
    
    private String createBNode() // TODO probably broken? Can you write bnodes in rdfa directly?
    {
        return "_:node" + (bnodeId++);
    }

    private void getNamespaces(Attributes attrs) {
        for (int i = 0; i < attrs.getLength(); i++) {
            String qname = attrs.getQName(i);
            String prefix = getPrefix(qname);
            if ("xmlns".equals(prefix)) {
                String pre = getLocal(prefix, qname);
                String uri = attrs.getValue(i);
                if (!settings.contains(Setting.ManualNamespaces) && pre.contains("_"))
                    continue; // not permitted
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
    private EvalContext context;

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
            String prefix = /*(localname.equals(qname))*/
                    (qname.indexOf(':') == -1 ) ? ""
                    : qname.substring(0, qname.indexOf(':'));
            if (settings.contains(Setting.ManualNamespaces)) {
                getNamespaces(arg3);
                if (prefix.length() != 0) {
                    arg0 = context.getNamespaceURI(prefix);
                    localname = localname.substring(prefix.length() + 1);
                }
            }
            StartElement e = eventFactory.createStartElement(
                    prefix, arg0, localname,
                    fromAttributes(arg3), null, context);

            if (literalCollector.isCollecting()) literalCollector.handleEvent(e);

            // If we are gathering XML we stop parsing
            if (!literalCollector.isCollectingXML()) context = parse(context, e);
        } catch (XMLStreamException ex) {
            throw new RuntimeException("Streaming issue", ex);
        }

    }

    public void endElement(String arg0, String localname, String qname) throws SAXException {
        //System.err.println("End element: " + arg0 + " " + arg1 + " " + arg2);
        if (literalCollector.isCollecting()) {
            String prefix = (localname.equals(qname)) ? ""
                    : qname.substring(0, qname.indexOf(':'));
            XMLEvent e = eventFactory.createEndElement(prefix, arg0, localname);
            literalCollector.handleEvent(e);
        }
        // If we aren't collecting an XML literal keep parsing
        if (!literalCollector.isCollectingXML()) context = context.parent;
    }

    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        if (literalCollector.isCollecting()) {
            XMLEvent e = eventFactory.createCharacters(String.valueOf(arg0, arg1, arg2));
            literalCollector.handleEvent(e);
        }
    }

    public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
        //System.err.println("Whitespace...");
        if (literalCollector.isCollecting()) {
            XMLEvent e = eventFactory.createIgnorableSpace(String.valueOf(arg0, arg1, arg2));
            literalCollector.handleEvent(e);
        }
    }

    public void processingInstruction(String arg0, String arg1) throws SAXException {
    }

    public void skippedEntity(String arg0) throws SAXException {
    }

    private Iterator fromAttributes(Attributes attributes) {
        List toReturn = new LinkedList();
        
        for (int i = 0; i < attributes.getLength(); i++) {
            String qname = attributes.getQName(i);
            String prefix = qname.contains(":") ? qname.substring(0, qname.indexOf(":")) : "";
            Attribute attr = eventFactory.createAttribute(
                    prefix, attributes.getURI(i),
                    attributes.getLocalName(i), attributes.getValue(i));

            if (!qname.equals("xmlns") && !qname.startsWith("xmlns:"))
                toReturn.add(attr);
        }
        
        return toReturn.iterator();
    }

    // 1.1 method

    private void parsePrefixes(String value, EvalContext context) {
        String[] parts = value.split("\\s+");
        for (int i = 0; i < parts.length; i += 2) {
            String prefix = parts[i];
            if (i + 1 < parts.length && prefix.endsWith(":")) {
                String prefixFix = prefix.substring(0, prefix.length() - 1);
                context.setPrefix(prefixFix, parts[i+1]);
                sink.addPrefix(prefixFix, parts[i+1]);
            }
        }
    }
    
    // SAX error handling
    
    public void warning(SAXParseException exception) throws SAXException {
        System.err.printf("Warning: %s\n", exception.getLocalizedMessage());
    }

    public void error(SAXParseException exception) throws SAXException {
        System.err.printf("Error: %s\n", exception.getLocalizedMessage());
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        System.err.printf("Fatal error: %s\n", exception.getLocalizedMessage());
    }
    
    // Coalesce utility functions. Useful in parsing.
    
    private static <T> T coalesce(T a, T b) {
        if (a != null && a != URIExtractor.NONE) return a;
        return b;
    }
    
    private static <T> T coalesce(T a, T b, T c) {
        if (a != null && a != URIExtractor.NONE) return a;
        if (b != null && b != URIExtractor.NONE) return b;
        return c;
    }
    
    private static <T> T coalesce(T a, T b, T c, T d) {
        if (a != null && a != URIExtractor.NONE) return a;
        if (b != null && b != URIExtractor.NONE) return b;
        if (c != null && c != URIExtractor.NONE) return c;
        return d;
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
