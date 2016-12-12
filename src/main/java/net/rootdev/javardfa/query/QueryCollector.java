/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa.query;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.vocabulary.RDF;
import java.util.*;
import java.util.Map.Entry;
import net.rootdev.javardfa.StatementSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryCollector implements StatementSink {

    final static Logger log = LoggerFactory.getLogger(QueryCollector.class);
    private static final Node FormType = NodeFactory.createURI("http://www.w3.org/1999/xhtml/vocab/#form");
    private static final TypeMapper TMapper = TypeMapper.getInstance();
    private final Map<String, Query> queries;
    private List<Triple> currentQuery;
    private String currentQueryName;
    private final Map<String, String> prefixMapping;

    public QueryCollector() {
        queries = new HashMap();
        prefixMapping = new HashMap();
    }

    public Map<String, Query> getQueries() {
        return Collections.unmodifiableMap(queries);
    }

    public void addLiteral(String arg0, String arg1, String arg2, String arg3,
            String arg4) {
        //log.info("Add literal");
        Node subject = getNode(arg0);
        Node predicate = getNode(arg1);
        Node object = getLiteralNode(arg2, arg3, arg4);
        addTriple(subject, predicate, object);
    }

    public void addObject(String arg0, String arg1, String arg2) {
        //log.info("Add object");
        Node subject = getNode(arg0);
        Node predicate = getNode(arg1);
        Node object = getNode(arg2);
        addTriple(subject, predicate, object);
    }

    private void addTriple(Node subject, Node predicate, Node object) {
        //log.info("Adding triple: " + subject + " " + predicate + " " + object);
        if (RDF.type.asNode().equals(predicate) &&
                FormType.equals(object)) {
            if (currentQueryName != null) {
                queries.put(currentQueryName, createQuery(currentQuery));
            }
            currentQueryName = subject.getURI();
            currentQuery = new LinkedList<Triple>();
            return;
        }
        if (currentQueryName == null) {
            return; // good idea? not sure...
        }
        currentQuery.add(Triple.create(subject, predicate, object));
    }

    private Node getLiteralNode(String arg2, String arg3, String arg4) {
        if (arg3 == null && arg4 == null) {
            return NodeFactory.createLiteral(arg2);
        } else if (arg4 == null) { // has lang
            return NodeFactory.createLiteral(arg2, arg3, false);
        } else { // has datatype
            return NodeFactory.createLiteral(arg2, null, TMapper.getSafeTypeByName(arg4));
        }
    }

    private Node getNode(String arg0) {
        if (arg0.startsWith("_:")) // BNode
        {
            return NodeFactory.createBlankNode(BlankNodeId.create(arg0.substring(2)));
        }
        if (arg0.startsWith("?")) // Var
        {
            return Var.alloc(arg0.substring(1));
        } else {
            return NodeFactory.createURI(arg0);
        }
    }

    public void end() {
        if (currentQueryName != null) {
            queries.put(currentQueryName, createQuery(currentQuery));
        }
    }

    public void start() {
    }

    public Query createQuery(List<Triple> triples) {
        log.info("Create query");
        Query query = new Query();
        ElementGroup body = new ElementGroup();
        for (Triple t : triples) {
            body.addTriplePattern(t);
        }
        // TODO make this switchable.
        Element pattern = new ElementNamedGraph(Var.alloc("graph"), body);
        query.setQueryPattern(pattern);
        query.addProjectVars(Collections.singleton("s"));
        //query.setQuerySelectType();
        Template templ = new Template(BasicPattern.wrap(triples));
        query.setQuerySelectType();
        query.setQueryResultStar(true);
        query.setConstructTemplate(templ);
        for (Entry<String, String> e: prefixMapping.entrySet())
            query.setPrefix(e.getKey(), e.getValue());
        return query;
    }

    public void addPrefix(String prefix, String uri) {
        prefixMapping.put(prefix, uri);
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