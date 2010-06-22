/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa;

import net.rootdev.javardfa.uri.IRIResolver;
import net.rootdev.javardfa.uri.URIExtractor;
import net.rootdev.javardfa.uri.URIExtractor10;
import net.rootdev.javardfa.uri.URIExtractor11;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;

/**
 * I use these in a few places. stuck here for simplicity
 *
 * @author pldms
 */
public class ParserFactory {

    public enum Format {

        HTML, XHTML;

        public static Format lookup(String format) {
            if ("xhtml".equalsIgnoreCase(format)) {
                return XHTML;
            }
            if ("html".equalsIgnoreCase(format)) {
                return HTML;
            }
            return null;
        }
    }

    /**
     *
     * @return An XMLReader with validation turned off
     * @throws SAXException
     */
    public static XMLReader createNonvalidatingReader() throws SAXException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return reader;
    }

    /**
     *
     * @return An HTML 5 XMLReader set up to by fairly forgiving.
     */
    public static XMLReader createHTML5Reader() {
        HtmlParser reader = new HtmlParser();
        reader.setXmlPolicy(XmlViolationPolicy.ALLOW);
        reader.setXmlnsPolicy(XmlViolationPolicy.ALLOW);
        reader.setMappingLangToXmlLang(false);
        return reader;
    }

    /**
     * Makes an XMLReader appropriate to the format, with an rdfa parser plumbed
     * to the StatementSink sink. Uses IRI resolver.
     *
     * @param sink
     * @param format
     * @return
     * @throws SAXException
     */
    public static XMLReader createReaderForFormat(StatementSink sink,
            Format format, Setting... settings) throws SAXException {
        return createReaderForFormat(sink, format, new IRIResolver(), settings);
    }

    /**
     * Makes an XMLReader appropriate to the format, with an rdfa parser plumbed
     * to the StatementSink sink.
     *
     * @param sink
     * @param format
     * @param resolver
     * @return
     * @throws SAXException
     */
    public static XMLReader createReaderForFormat(StatementSink sink,
            Format format, Resolver resolver, Setting... settings) throws SAXException {
        XMLReader reader = getReader(format);
        boolean is11 = false;
        for (Setting setting: settings) if (setting == Setting.OnePointOne) is11 = true;
        URIExtractor extractor = (is11) ?
            new URIExtractor11(resolver) : new URIExtractor10(resolver);
        ProfileCollector profileCollector = (is11) ?
            new SimpleProfileCollector() : ProfileCollector.EMPTY_COLLECTOR ;
        Parser parser = getParser(format, sink, extractor, profileCollector);
        for (Setting setting: settings) parser.enable(setting);
        reader.setContentHandler(parser);
        return reader;
    }

    private static XMLReader getReader(Format format) throws SAXException {
        switch (format) {
            case XHTML:
                return ParserFactory.createNonvalidatingReader();
            default:
                return ParserFactory.createHTML5Reader();
        }
    }

    private static Parser getParser(Format format, StatementSink sink,
            URIExtractor extractor, ProfileCollector profileCollector) {
        return getParser(format, sink, XMLOutputFactory.newInstance(), 
                XMLEventFactory.newInstance(), extractor, profileCollector);
    }

    private static Parser getParser(Format format, StatementSink sink,
            XMLOutputFactory outputFactory, XMLEventFactory eventFactory,
            URIExtractor extractor, ProfileCollector profileCollector) {
        switch (format) {
            case XHTML:
                return new Parser(sink, outputFactory, eventFactory, extractor, profileCollector);
            default:
                Parser p = new Parser(sink, outputFactory, eventFactory, extractor, profileCollector);
                p.enable(Setting.ManualNamespaces);
                return p;
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
