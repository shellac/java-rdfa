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

    String expandCURIE(StartElement element, String value, EvalContext context);

    String expandSafeCURIE(StartElement element, String value, EvalContext context);

    String getURI(StartElement element, Attribute attr, EvalContext context);

    List<String> getURIs(StartElement element, Attribute attr, EvalContext context);

}
