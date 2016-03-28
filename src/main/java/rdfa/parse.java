/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */

package rdfa;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import net.rootdev.javardfa.Version;

/**
 * Simple command line tool. Uses Jena, and doesn't stream, so output
 * is much nicer than simpleparse.
 *
 * @author pldms
 */
public class parse {

    public static void main(String... args) throws ClassNotFoundException {
        if (args.length == 0) usage();
        if ("--version".equals(args[0]) || "-v".equals(args[0])) version();

        // Ensure hooks run
        Class.forName("net.rootdev.javardfa.RDFaReader");

        String format = "XHTML";
        boolean getFormat = false;

        List<String> uris = new LinkedList<String>();

        for (String arg: args) {
            if (getFormat) { format = arg; getFormat = false; }
            else if ("--help".equalsIgnoreCase(arg)) usage();
            else if ("--format".equalsIgnoreCase(arg)) getFormat = true;
            else uris.add(arg);
        }

        if (getFormat) usage();

        Model m = ModelFactory.createDefaultModel();
        FileManager fm = FileManager.get();
        for (String uri: uris) {
            InputStream in = fm.open(uri);
            m.read(in, uri, format);
        }
        m.write(System.out, "TTL");
    }

    private static void usage() {
        System.err.println("rdfa.parse [--version] [--format XHTML|HTML] <url> [...]");
        System.exit(0);
    }

    private static void version() {
        System.err.println(Version.get());
        System.exit(0);
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
