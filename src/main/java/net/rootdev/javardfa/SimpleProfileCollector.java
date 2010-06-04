/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

/**
 *
 * @author pldms
 */
public class SimpleProfileCollector implements ProfileCollector {

    public void getProfile(String profileURI, EvalContext context) {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    static class SimpleCollector implements StatementSink {
        private final EvalContext context;

        SimpleCollector(EvalContext context) {
            this.context = context;
        }

        public void start() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void end() {}

        // Doesn't use objects
        public void addObject(String subject, String predicate, String object) {}

        public void addLiteral(String subject, String predicate, String lex, String lang, String datatype) {
            if (predicate.equals(ProfileCollector.prefix)) {

            }
        }

        public void addPrefix(String prefix, String uri) {}

    }
}
