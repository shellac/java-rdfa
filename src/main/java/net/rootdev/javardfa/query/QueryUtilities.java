/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa.query;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.rootdev.javardfa.Parser;
import net.rootdev.javardfa.ParserFactory;
import net.rootdev.javardfa.ParserFactory.Format;
import net.rootdev.javardfa.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * Some useful functions concerning pages with variables
 *
 * @author pldms
 */
public class QueryUtilities {

    final static Logger log = LoggerFactory.getLogger(QueryUtilities.class);
    public final static QuerySolution NoResult = new QuerySolutionMap();

    /**
     * Grab simple (BGP) queries from an html document. Over named graphs currently.
     *
     * @param format document format
     * @param source document id
     * @return
     */
    public static Map<String, Query> makeQueries(Format format, String source) throws SAXException, IOException {
        QueryCollector qc = new QueryCollector();
        XMLReader reader = ParserFactory.createReaderForFormat(qc, format);
        ((Parser) reader.getContentHandler()).enable(Setting.FormMode);
        if (source.matches("file:/[^/][^/].*")) source = source.replaceFirst("file:/", "file:///");
        reader.parse(source);
        return qc.getQueries();
    }

    // TODO: currently this won't work! Need to add to a named graph
    /**
     * Simple method to help rebinding data to form. Currently too simple.
     * @param model Contains data to rebind
     * @param query Extracted from the form above
     * @return Name / node bindings
     */
    public static QuerySolution extractBinding(Model model, Query query) {
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet res = qe.execSelect();
        final QuerySolution toReturn = (res.hasNext()) ?
            res.next() : NoResult; // I will never use null again
        if (res.hasNext()) log.warn("More than one available binding");
        qe.close();
        return toReturn;
    }

    /**
     * Given some bindings and a form create a model. Intended use is handling
     * the result of form submission.
     *
     * @param query The form
     * @param bindings Submitted bindings
     * @return Bindings applied to the query
     */
    public static Model bind(Query query, Map<String, String> bindings) {
        List<Triple> triples = pullTriples(query);
        List<Triple> boundTriples = new LinkedList<Triple>();
        Model model = ModelFactory.createDefaultModel();
        for (Triple t: triples) {
            Node s = bind(t.getSubject(), bindings);
            Node p = bind(t.getPredicate(), bindings);
            Node o = bind(t.getObject(), bindings);
            Triple nt = Triple.create(s, p, o);
            model.add(model.asStatement(nt));
        }
        return model;
    }

    /**
     * Collect all triples from a query body
     * @param query
     * @return
     */
    private static List<Triple> pullTriples(Query query) {
        List<Triple> triples = new LinkedList<Triple>();
        ElementWalker.walk(query.getQueryPattern(), new TripleCollector(triples));
        return triples;
    }

    private static Node bind(Node object, Map<String, String> bindings) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static class TripleCollector extends ElementVisitorBase {
        private final List<Triple> triples;

        private TripleCollector(List<Triple> triples) {
            this.triples = triples;
        }

        @Override
        public void visit(ElementTriplesBlock el) {
            triples.addAll(el.getPattern().getList());
        }

    }
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
