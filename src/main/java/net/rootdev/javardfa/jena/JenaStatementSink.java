/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa.jena;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping.IllegalPrefixException;
import java.util.HashMap;
import java.util.Map;
import net.rootdev.javardfa.StatementSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Damian Steer <pldms@mac.com>
 */
public class JenaStatementSink implements StatementSink {

    private static Logger log = LoggerFactory.getLogger(JenaStatementSink.class);
    private final Model model;
    private Map<String, Resource> bnodeLookup;

    public JenaStatementSink(Model model) {
        this.model = model;
    }

    //@Override
    public void start() {
        bnodeLookup = new HashMap<String, Resource>();
    }

    //@Override
    public void end() {
        bnodeLookup = null;
    }

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
        if (lang == null && datatype == null) {
            o = model.createLiteral(lex);
        } else if (lang != null) {
            o = model.createLiteral(lex, lang);
        } else {
            o = model.createTypedLiteral(lex, datatype);
        }
        model.add(s, p, o);
    }

    private Resource getResource(String res) {
        if (res.startsWith("_:")) {
            if (bnodeLookup.containsKey(res)) {
                return bnodeLookup.get(res);
            }
            Resource bnode = model.createResource();
            bnodeLookup.put(res, bnode);
            return bnode;
        } else {
            return model.createResource(res);
        }
    }

    public void addPrefix(String prefix, String uri) {
        try {
            model.setNsPrefix(prefix, uri);
        } catch (IllegalPrefixException e) {
            log.warn("Bad prefix, continuing.", e);
        }
    }

    public void setBase(String base) {}
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
