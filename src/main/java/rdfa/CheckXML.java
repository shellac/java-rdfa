/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rdfa;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class CheckXML {

    final static String nsURI = "http://example.com/";

    public static void main(String... args) throws XMLStreamException {
        XMLOutputFactory outF = XMLOutputFactory.newFactory();
        System.err.println(outF.getClass());
        outF.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
        XMLStreamWriter out = outF.createXMLStreamWriter(System.out);
        //out.setDefaultNamespace(nsURI);
        out.writeStartElement("", "element", nsURI);
        out.writeAttribute("attribute", "value");
        out.writeAttribute("attribute2", "value");
        out.writeCharacters("");
        out.writeEndElement();
        out.close();
    }

}
