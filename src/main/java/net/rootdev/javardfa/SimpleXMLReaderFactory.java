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
public class SimpleXMLReaderFactory {

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
}
