/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */

package net.rootdev.javardfa.output;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.regex.Pattern;
import net.rootdev.javardfa.StatementSink;


/**
 * A pretty ropey NTriple serialiser.
 * Advantages: streams, no dependencies.
 *
 * @author pldms
 */
public class NTripleSink implements StatementSink {
    protected final PrintWriter out;
    protected final String[] comments;

    public NTripleSink(OutputStream os, String... comments) throws UnsupportedEncodingException {
        this(new OutputStreamWriter(os, "US-ASCII"), comments); // N-Triples is 7-bit ascii
    }

    public NTripleSink(Writer writer, String... comments) {
        this.out = new PrintWriter(writer);
        this.comments = comments;
    }

    public void start() {
        for (String line: comments) {
            out.print("# ");
            out.println(line);
        }
    }

    public void end() {
        out.flush();
    }

    public void addObject(String subject, String predicate, String object) {
        out.print(toNode(subject));
        out.print(toNode(predicate));
        out.print(toNode(object));
        out.println(".");
    }

    public void addLiteral(String subject, String predicate, String lex, String lang, String datatype) {
        out.print(toNode(subject));
        out.print(toNode(predicate));
        out.print(toLiteral(lex, lang, datatype));
        out.println(".");
    }

    public void addPrefix(String prefix, String uri) {}

    protected final String toNode(String node) {
        if (node.startsWith("_:") || node.startsWith("?"))
            return node + " ";
        return "<" + node + "> ";
    }

    protected final String toLiteral(String lex, String lang, String datatype) {
        if (lang != null && lang.length() != 0)
            return quote(lex) + "@" + lang + " ";
        if (datatype != null)
            return quote(lex) + "^^<" + datatype + "> ";
        return quote(lex) + " ";
    }

    private Pattern quotePattern = Pattern.compile("\"");
    protected final String quote(String lex) {
        return "\"" + encode(lex) + "\"";
    }

    protected final String encode(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            int c = s.codePointAt(i);
            if (c <= 8) b.append(enc(c));
            else if (c == '\t') b.append("\\t");
            else if (c == '\n') b.append("\\n");
            else if (c == '\r') b.append("\\r");
            else if (c == '"')  b.append("\\\"");
            else if (c == '\\') b.append("\\\\");
            else if (c <= 127)  b.appendCodePoint(c);
            else if (c <= 0xFFFF) b.append(enc(c));
            else b.append(longenc(c));
        }
        return b.toString();
    }

    protected String enc(int codepoint) {
        return String.format("\\u%04x", codepoint);
    }

    protected String longenc(int codepoint) {
        return String.format("\\U%08x", codepoint);
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