/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */

package net.rootdev.javardfa;

/**
 * @author Damian Steer <pldms@mac.com>
 */

public interface StatementSink
{
    /**
     *
     */
    public void setBase(String base);

    /**
     * Begin parsing
     */
    public void start();

    /**
     * Complete parsing
     */
    public void end();

    /**
     * Add statement with non-literal object.
     * Blank nodes begin with _:, variables with ?, otherwise IRI
     * @param subject Subject of triple
     * @param predicate Predicate
     * @param object Object
     */
    public void addObject(String subject, String predicate, String object);

    /**
     * Add statement with a literal object.
     * As above, blank nodes begin with _:, variables with ?, otherwise IRI
     * @param subject Subject of triple
     * @param predicate Predicate
     * @param lex Lexical form
     * @param lang Language (may be null)
     * @param datatype Datatype IRI (may be null)
     */
    public void addLiteral(String subject, String predicate, String lex, String lang, String datatype);

    /**
     * Add a prefix mapping.
     * @param prefix
     * @param uri
     */
    public void addPrefix(String prefix, String uri);
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