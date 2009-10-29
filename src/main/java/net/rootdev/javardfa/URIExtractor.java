/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

/**
 *
 * @author pldms
 */
public class URIExtractor {
    final private IRIFactory iriFact;
    private Constants consts;
    private Set<Setting> settings;

    public URIExtractor(IRIFactory iriFact) {
        this.iriFact = iriFact;
    }

    public void setSettings(Set<Setting> settings) { this.settings = settings; }

    public void setConstants(Constants consts) { this.consts = consts; }

    public String getURI(String base, StartElement element, Attribute attr) {
        QName attrName = attr.getName();
        if (attrName.equals(consts.href) || attrName.equals(consts.src)) // A URI
        {
            if (attr.getValue().length() == 0) {
                return base;
            }
            IRI uri = iriFact.construct(base);
            IRI resolved = uri.resolve(attr.getValue());
            return resolved.toString();
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
            boolean isSpecial = (settings.contains(Setting.ManualNamespaces)) ? 
                consts.SpecialRels.contains(curie.toLowerCase()) :
                consts.SpecialRels.contains(curie);
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

            IRI uri = iriFact.construct(base);
            IRI resolved = uri.resolve(value);
            return resolved.toString();
        }
    }
}
