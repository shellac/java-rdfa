/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

/**
 *
 * @author pldms
 */
public interface ProfileCollector {

    public final static String NS = "http://www.w3.org/ns/rdfa#";
    public final static String uri = NS + "uri";
    public final static String term = NS + "term";
    public final static String prefix = NS + "prefix";

    void getProfile(String profileURI, EvalContext context);

}
