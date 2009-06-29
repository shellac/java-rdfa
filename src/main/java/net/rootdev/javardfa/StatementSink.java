/*
 * (c) 2009
 * Damian Steer <mailto:pldms@mac.com>
 */

package net.rootdev.javardfa;

/**
 *
 * @author pldms
 */
public interface StatementSink
{
    public void start();
    public void end();
    public void addObject(String subject, String predicate, String object);
    public void addLiteral(String subject, String predicate, String lex, String lang, String datatype);
}
