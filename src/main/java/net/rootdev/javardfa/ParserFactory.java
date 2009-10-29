/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rootdev.javardfa;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

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

    public static XMLReader createNonvalidatingReader() throws SAXException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return reader;
    }

    public static XMLReader createHTML5Reader() {
        HtmlParser reader = new HtmlParser();
        reader.setXmlPolicy(XmlViolationPolicy.ALLOW);
        reader.setXmlnsPolicy(XmlViolationPolicy.ALLOW);
        reader.setMappingLangToXmlLang(false);
        return reader;
    }

    public static XMLReader createReaderForFormat(StatementSink sink, Format format) throws SAXException {
        XMLReader reader = getReader(format);
        Parser parser = getParser(format, sink);
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

    private static Parser getParser(Format format, StatementSink sink) {
        switch (format) {
            case XHTML:
                return new Parser(sink);
            default:
                Parser p = new Parser(sink);
                p.enable(Setting.ManualNamespaces);
                return p;
        }
    }
}
