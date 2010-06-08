/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa.uri;

import java.util.List;
import java.util.Set;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import net.rootdev.javardfa.EvalContext;
import net.rootdev.javardfa.Setting;

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

    String resolveURI(String uri, EvalContext context);
}
