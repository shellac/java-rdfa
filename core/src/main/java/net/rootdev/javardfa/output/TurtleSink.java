/*
 * (c) Copyright 2010 University of Bristol
 * All rights reserved.
 * [See end of file]
 */

package net.rootdev.javardfa.output;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;


/**
 * A pretty ropey turtle serialiser.
 * Advantages: streams, no dependencies.
 *
 * @author pldms
 */
public class TurtleSink extends NTripleSink {

    private String currentSubject = null;
    private String currentPredicate = null;

    public TurtleSink(OutputStream os, String... comments) throws UnsupportedEncodingException {
        super(new OutputStreamWriter(os, "UTF-8"), comments);
    }

    public TurtleSink(Writer writer, String... comments) {
        super(writer, comments);
    }

    @Override
    public void end() {
        out.println(".");
        super.end();
    }

    @Override
    public void addObject(String subject, String predicate, String object) {
        emitTriple(subject, predicate, toNode(object));
    }

    @Override
    public void addLiteral(String subject, String predicate, String lex, String lang, String datatype) {
        emitTriple(subject, predicate, toLiteral(lex, lang, datatype));
    }

    private void emitTriple(String subject, String predicate, String objectEncoded) {
        if (subject.equals(currentSubject)) { // We can at least ';'
            if (predicate.equals(currentPredicate)) { // We can ','
                out.println(",");
                out.print("\t\t");
                out.print(objectEncoded);
            } else {
                out.println(";");
                out.print("\t");
                out.println(toNode(predicate));
                out.print("\t\t");
                out.print(objectEncoded);
                currentPredicate = predicate;
            }
        } else {
            if (currentSubject != null) out.println(".");
            out.println(toNode(subject));
            out.print("\t");
            out.println(toNode(predicate));
            out.print("\t\t");
            out.print(objectEncoded);
            currentPredicate = predicate;
            currentSubject = subject;
        }
    }

    @Override
    protected final String enc(int codepoint) {
        return new String( new int[] {codepoint},0,1);
    }

    @Override
    protected final String longenc(int codepoint) {
        return new String( new int[] {codepoint},0,1);
    }
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