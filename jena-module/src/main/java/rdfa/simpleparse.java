/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */

package rdfa;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import net.rootdev.javardfa.ParserFactory;
import net.rootdev.javardfa.ParserFactory.Format;
import net.rootdev.javardfa.StatementSink;
import net.rootdev.javardfa.output.TurtleSink;
import net.rootdev.javardfa.uri.URIResolver;
import net.rootdev.javardfa.Version;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Simple command line tool
 *
 * @author pldms
 */
public class simpleparse {

    public static void main(String... args) throws ClassNotFoundException, MalformedURLException, IOException, SAXException {
        if (args.length == 0) usage();
        if ("--version".equals(args[0]) || "-v".equals(args[0])) version();


        Format format = Format.XHTML;
        boolean getFormat = false;

        List<String> uris = new LinkedList<String>();

        for (String arg: args) {
            if (getFormat) { format = Format.lookup(arg); getFormat = false; }
            else if ("--help".equalsIgnoreCase(arg)) usage();
            else if ("--format".equalsIgnoreCase(arg)) getFormat = true;
            else uris.add(arg);
        }

        if (format == null) unknownFormat();
        if (getFormat) usage();

        for (String uri: uris) {
            StatementSink sink = new TurtleSink(System.out);
            XMLReader reader = ParserFactory.createReaderForFormat(sink, format, new URIResolver());
            reader.parse(uri);
        }
    }

    private static void usage() {
        System.err.println("rdfa.simpleparse [--version] [--format XHTML|HTML] <url> [...]");
        System.exit(0);
    }

    private static void version() {
        System.err.println(Version.get());
        System.exit(0);
    }

    private static void unknownFormat() {
        System.err.println("Unknown format");
        usage();
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
