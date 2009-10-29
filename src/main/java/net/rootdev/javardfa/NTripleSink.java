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

    public NTripleSink(OutputStream os) throws UnsupportedEncodingException {
        this(new OutputStreamWriter(os, "utf-8"));
    }

    public NTripleSink(Writer writer) {
        this.out = new PrintWriter(writer);
    }

    public void start() { }

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
            return quote(lex) + "^^" + datatype + " ";
        return quote(lex) + " ";
    }

    private Pattern quotePattern = Pattern.compile("\"");
    protected final String quote(String lex) {
        // Oh boy, when quotes go nuts
        String escaped = quotePattern.matcher(lex).replaceAll("\\\\\"");
        return "\"" + escaped + "\"";
    }
}
