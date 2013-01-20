/*
 * (c) Copyright 2010 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author pldms
 */
public class SimpleProfileCollector implements ProfileCollector {

    public void getProfile(String profileURI, EvalContext context) {
        System.err.println("!! GET PROFILE?? " + profileURI);
        if (true) return;
        try {
            XMLReader reader =
                    ParserFactory.createReaderForFormat(
                        new SimpleCollector(context),
                        ParserFactory.Format.XHTML,
                        Setting.OnePointOne);
            reader.parse(profileURI);
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    static class SimpleCollector implements StatementSink {
        private final EvalContext context;
        private final Map<String, Value> subjToVals;

        SimpleCollector(EvalContext context) {
            this.context = context;
            this.subjToVals = new HashMap<String, Value>();
        }

        public void start() {}

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

        public void setBase(String base) {}

    }

    static final class Value {
        String prefix;
        String term;
        String uri;
    }
}


/*
 * (c) Copyright 2010 University of Bristol
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