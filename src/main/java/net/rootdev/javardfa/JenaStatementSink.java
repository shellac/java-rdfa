/*
 * (c) 2009
 * Damian Steer <mailto:pldms@mac.com>
 */
package net.rootdev.javardfa;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pldms
 */
public class JenaStatementSink implements StatementSink {

    private final Model model;
    private Map<String, Resource> bnodeLookup;

    public JenaStatementSink(Model model) {
        this.model = model;
    }

    //@Override
    public void start() { bnodeLookup = new HashMap<String, Resource>(); }

    //@Override
    public void end() { bnodeLookup = null; }

    //@Override
    public void addObject(String subject, String predicate, String object) {
        Resource s = getResource(subject);
        Property p = model.createProperty(predicate);
        Resource o = getResource(object);
        model.add(s, p, o);
    }

    //@Override
    public void addLiteral(String subject, String predicate, String lex, String lang, String datatype) {
        Resource s = getResource(subject);
        Property p = model.createProperty(predicate);
        Literal o;
        if (lang == null && datatype == null)
            o = model.createLiteral(lex);
        else if (lang != null)
            o = model.createLiteral(lex, lang);
        else
            o = model.createTypedLiteral(lex, datatype);
        model.add(s, p, o);
    }

    private Resource getResource(String res) {
        if (res.startsWith("_:")) {
            if (bnodeLookup.containsKey(res)) return bnodeLookup.get(res);
            Resource bnode = model.createResource();
            bnodeLookup.put(res, bnode);
            return bnode;
        } else {
            return model.createResource(res);
        }
    }
}
