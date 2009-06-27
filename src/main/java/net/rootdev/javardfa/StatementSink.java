/*
 * (c) 2009
 * Damian Steer <mailto:pldms@mac.com>
 */

package net.rootdev.javardfa;

import com.hp.hpl.jena.graph.Node;

/**
 *
 * @author pldms
 */
public interface StatementSink
{
    public void start();
    public void end();
    public void add(Node s, Node p, Node o);
}
