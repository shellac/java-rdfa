/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;

class EvalContext implements NamespaceContext {

    EvalContext parent;
    String base;
    String parentSubject;
    String parentObject;
    String language;
    String vocab;
    List<String> forwardProperties;
    List<String> backwardProperties;
    Map<String, String> prefixToUri = new HashMap<String, String>();

    protected EvalContext(String base) {
        super();
        this.base = base;
        this.parentSubject = base;
        this.forwardProperties = new LinkedList<String>();
        this.backwardProperties = new LinkedList<String>();
    }

    public EvalContext(EvalContext toCopy) {
        super();
        this.base = toCopy.base;
        this.parentSubject = toCopy.parentSubject;
        this.parentObject = toCopy.parentObject;
        this.language = toCopy.language;
        this.forwardProperties = new LinkedList<String>(toCopy.forwardProperties);
        this.backwardProperties = new LinkedList<String>(toCopy.backwardProperties);
        this.parent = toCopy;
        this.vocab = toCopy.vocab;
    }

    public void setBase(String abase) {
        // This is very dodgy. We want to check if ps and po have been changed
        // from their typical values (base).
        // Base changing happens very late in the day when we're streaming, and
        // it is very fiddly to handle
        boolean setPS = parentSubject == base;
        boolean setPO = parentObject == base;

        if (abase.contains("#")) {
            this.base = abase.substring(0, abase.indexOf("#"));
        } else {
            this.base = abase;
        }

        if (setPS) this.parentSubject = base;
        if (setPO) this.parentObject = base;
        
        if (parent != null) {
            parent.setBase(base);
        }
    }

    @Override
    public String toString() {
        return
            String.format("[\n\tBase: %s\n\tPS: %s\n\tPO: %s\n\tlang: %s\n\tIncomplete: -> %s <- %s\n]",
                base, parentSubject, parentObject, language,
                forwardProperties.size(), backwardProperties.size()
                );
    }

    public void setNamespaceURI(String prefix, String uri) {
        if (uri.length() == 0) {
            uri = base;
        }
        prefixToUri.put(prefix, uri);
    }

    public String getNamespaceURI(String prefix) {
        if (prefixToUri.containsKey(prefix)) {
            return prefixToUri.get(prefix);
        } else if (parent != null) {
            return parent.getNamespaceURI(prefix);
        } else {
            return null;
        }
    }

    public String getPrefix(String uri) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Iterator getPrefixes(String uri) {
        throw new UnsupportedOperationException("Not supported yet.");
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