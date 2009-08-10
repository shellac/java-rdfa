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
    List<String> forwardProperties;
    List<String> backwardProperties;
    Map<String, String> prefixToUri = new HashMap<String, String>();
    boolean original;
    boolean langIsLang = false;

    protected EvalContext(String base) {
        super();
        this.base = base;
        this.parentSubject = base;
        this.forwardProperties = new LinkedList<String>();
        this.backwardProperties = new LinkedList<String>();
        original = true;
    }

    public EvalContext(EvalContext toCopy) {
        super();
        this.base = toCopy.base;
        this.parentSubject = toCopy.parentSubject;
        this.parentObject = toCopy.parentObject;
        this.language = toCopy.language;
        this.forwardProperties = new LinkedList<String>(toCopy.forwardProperties);
        this.backwardProperties = new LinkedList<String>(toCopy.backwardProperties);
        this.langIsLang = toCopy.langIsLang;
        this.original = false;
        this.parent = toCopy;
    }

    public void setBase(String abase) {
        if (abase.contains("#")) {
            this.base = abase.substring(0, abase.indexOf("#"));
        } else {
            this.base = abase;
        }
        // Not great, but passes tests.
        // We want to say: if parentSubject hasn't been changed, it's base
        if (this.original) {
            this.parentSubject = this.base;
        }
        if (parent != null) {
            parent.setBase(base);
        }
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
