package net.rootdev.javardfa;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.TemplateGroup;
import com.hp.hpl.jena.vocabulary.RDF;
import java.util.Map.Entry;

public class QueryCollector implements StatementSink {

    final static Logger log = LoggerFactory.getLogger(QueryCollector.class);
    private static final Node FormType = Node.createURI("http://www.w3.org/1999/xhtml/vocab/#form");
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
            return Node.createLiteral(arg2);
        } else if (arg4 == null) { // has lang
            return Node.createLiteral(arg2, arg3, false);
        } else { // has datatype
            return Node.createLiteral(arg2, null, TMapper.getSafeTypeByName(arg4));
        }
    }

    private Node getNode(String arg0) {
        if (arg0.startsWith("_:")) // BNode
        {
            return Node.createAnon(AnonId.create(arg0.substring(2)));
        }
        if (arg0.startsWith("?")) // Var
        {
            return Var.alloc(arg0.substring(1));
        } else {
            return Node.createURI(arg0);
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
        TemplateGroup templ = new TemplateGroup();
        for (Triple triple : triples) {
            templ.addTriple(triple);
        }
        query.setQueryConstructType();
        query.setConstructTemplate(templ);
        for (Entry<String, String> e: prefixMapping.entrySet())
            query.setPrefix(e.getKey(), e.getValue());
        return query;
    }

    public void addPrefix(String prefix, String uri) {
        prefixMapping.put(prefix, uri);
    }
}
