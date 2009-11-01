/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.regex.Pattern;


/**
 *
 * @author pldms
 */
public class NTripleSink implements StatementSink {
    private final PrintWriter out;
    private final String[] comments;

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
        if (lang != null)
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

    protected final String enc(int codepoint) {
        return String.format("\\u%04x", codepoint);
    }

    protected final String longenc(int codepoint) {
        return String.format("\\u%08x", codepoint);
    }
}
