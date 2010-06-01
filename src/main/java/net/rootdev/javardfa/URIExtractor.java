/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import java.util.List;
import java.util.Set;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

/**
 *
 * @author pldms
 */
public interface URIExtractor {

    void setSettings(Set<Setting> settings);

    String expandCURIE(StartElement element, String value);

    String expandSafeCURIE(String base, StartElement element, String value);

    String getURI(String base, StartElement element, Attribute attr);

    List<String> getURIs(String base, StartElement element, Attribute attr);

}
