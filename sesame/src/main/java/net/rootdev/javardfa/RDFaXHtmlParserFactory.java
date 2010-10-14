/*
 *  New BSD license: http://opensource.org/licenses/bsd-license.php
 *
 *  Copyright (c) 2009 Sun Microsystems, Inc.
 *  901 San Antonio Road, Palo Alto, CA 94303 USA.
 *  All rights reserved.
 *
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package net.rootdev.javardfa;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import java.nio.charset.Charset;

/**
 * 
 * @author hjs
 */
public class RDFaXHtmlParserFactory implements RDFParserFactory {

    public static final RDFFormat rdfa_xhtml_Format;
    static {
        rdfa_xhtml_Format = new RDFFormat("rdfa-xhtml",
                "application/xhtml+xml", Charset.forName("UTF-8"), "xhtml",
                true, false);
        RDFFormat.register(rdfa_xhtml_Format);
    }

    /* (non-Javadoc)
     * @see org.openrdf.rio.RDFParserFactory#getRDFFormat()
     */
    @Override
    public RDFFormat getRDFFormat() {
        return rdfa_xhtml_Format;
    }

    /* (non-Javadoc)
     * @see org.openrdf.rio.RDFParserFactory#getParser()
     */
    @Override
    public RDFParser getParser() {
        return new SesameRDFaParser.XHTMLRDFaParser();
    }

}
