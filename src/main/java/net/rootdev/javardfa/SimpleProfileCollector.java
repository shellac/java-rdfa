/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author pldms
 */
public class SimpleProfileCollector implements ProfileCollector {

    public void getProfile(String profileURI, EvalContext context) {
        try {
            XMLReader reader =
                    ParserFactory.createReaderForFormat(
                        new SimpleCollector(context),
                        ParserFactory.Format.XHTML,
                        Setting.OnePointOne);
            reader.parse(profileURI);
        } catch (SAXException ex) {

        } catch (IOException ex) {

        }
    }

    static class SimpleCollector implements StatementSink {
        private final EvalContext context;
        private final Map<String, Value> subjToVals;

        SimpleCollector(EvalContext context) {
            this.context = context;
            this.subjToVals = new HashMap<String, Value>();
        }

        public void start() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void end() {}

        // Doesn't use objects
        public void addObject(String subject, String predicate, String object) {}

        public void addLiteral(String subject, String predicate, String lex, String lang, String datatype) {
            Value val = subjToVals.get(subject);
            if (val == null && ( predicate.equals(ProfileCollector.prefix) ||
                    predicate.equals(ProfileCollector.term) ||
                    predicate.equals(ProfileCollector.uri))) {
                val = new Value();
                subjToVals.put(subject, val);
            }

            if (val == null) return;
            
            if (predicate.equals(ProfileCollector.prefix)) {
                if (val.uri != null) context.setPrefix(lex, val.uri);
                else val.prefix = lex;
            } else if (predicate.equals(ProfileCollector.term)) {
                if (val.uri != null) context.setTerm(lex, val.uri);
                else val.term = lex;
            } else if (predicate.equals(ProfileCollector.uri)) {
                if (val.prefix != null) context.setPrefix(val.prefix, lex);
                else if (val.term != null) context.setTerm(val.term, lex);
                else val.uri = lex;
            }
        }

        public void addPrefix(String prefix, String uri) {}

    }

    static final class Value {
        String prefix;
        String term;
        String uri;
    }
}
