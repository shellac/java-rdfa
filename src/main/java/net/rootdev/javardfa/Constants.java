/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author pldms
 */
public class Constants {
    final static List<String> _allowed = Arrays.asList(
            "alternate", "appendix", "bookmark", "cite",
            "chapter", "contents", "copyright", "first",
            "glossary", "help", "icon", "index", "last",
            "license", "meta", "next", "p3pv1", "prev",
            "collection", "role", "section", "stylesheet",
            "subsection", "start", "top", "up");
    final Set<String> SpecialRels = new HashSet<String>(_allowed);
    // Suggestion: switch this for object produced by factory that matches QNames
    // we can then en-slacken if needed by passing in different factory etc
    final QName about = new QName("about"); // safe
    final QName resource = new QName("resource"); // safe
    final QName href = new QName("href"); // URI
    final QName src = new QName("src"); // URI
    final QName property = new QName("property"); // CURIE
    final QName datatype = new QName("datatype"); // CURIE
    final QName typeof = new QName("typeof"); // CURIE
    final QName rel = new QName("rel"); // Link types and CURIES
    final QName rev = new QName("rev"); // Link type and CURIES
    final QName content = new QName("content");
    final QName xmllang = new QName("http://www.w3.org/XML/1998/namespace", "lang", "xml");
    final QName lang = new QName("lang");
    final QName fakeXmlLang = new QName("xml:lang");
    final QName base = new QName("http://www.w3.org/1999/xhtml", "base");
    final QName head = new QName("http://www.w3.org/1999/xhtml", "head");
    final QName body = new QName("http://www.w3.org/1999/xhtml", "body");
    // Hack bits
    final QName input = new QName("input");
    final QName name = new QName("name");
    final QName form = new QName("form");
    final Collection<String> rdfType = Collections.singleton("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    final String xmlLiteral = "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral";
}
