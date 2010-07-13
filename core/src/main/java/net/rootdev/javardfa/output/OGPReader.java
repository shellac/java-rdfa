/*
 * (c) Copyright 2010 University of Bristol
 * All rights reserved.
 * [See end of file]
 */

package net.rootdev.javardfa.output;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.rootdev.javardfa.ParserFactory;
import net.rootdev.javardfa.ParserFactory.Format;
import net.rootdev.javardfa.StatementSink;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * @author pldms
 */
public class OGPReader implements StatementSink {

    static final String NS = "http://opengraphprotocol.org/schema/";
    static final int NSlen = NS.length();
    private String base;
    private final Map<String, String> content = new HashMap<String, String>();

    public void start() {}

    public void end() {}

    public void addObject(String subject, String predicate, String object) {
        collect(subject, predicate, object);
    }

    public void addLiteral(String subject, String predicate, String lex, String lang, String datatype) {
        collect(subject, predicate, lex);
    }

    public void addPrefix(String prefix, String uri) {}

    public void setBase(String base) {
        this.base = base;
    }

    private void collect(String subject, String predicate, String value) {
        if (!subject.equals(base)) return;
        if (predicate.startsWith(NS)) content.put(predicate.substring(NSlen), value);
        else content.put(predicate, value);
    }

    public Map<String, String> getContent() { return content; }

    /**
     * A rudimentary Open Graph Protocol parser
     * @param url Source to parse
     * @param format HTML or XHTML
     * @return Map from key to value. For OGP properties the key is simple (e.g. email)
     * @throws SAXException
     * @throws IOException
     */
    public static Map<String, String> getOGP(String url, Format format) throws SAXException, IOException {
        OGPReader reader = new OGPReader();
        XMLReader parser = ParserFactory.createReaderForFormat(reader, format);
        parser.parse(new InputSource(url));
        return reader.getContent();
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