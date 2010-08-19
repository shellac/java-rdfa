/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import java.nio.charset.Charset;
import org.kohsuke.MetaInfServices;

/**
 *
 * @author hjs
 */
@MetaInfServices
public class RDFaXHtmlParserFactory implements RDFParserFactory {

   public static final RDFFormat rdfa_xhtml_Format;
   static {
      rdfa_xhtml_Format = new RDFFormat("rdfa-xhtml", "application/xhtml+xml",
              Charset.forName("UTF-8"), "xhtml", true, false);
      RDFFormat.register(rdfa_xhtml_Format);
   }

   @Override
   public RDFFormat getRDFFormat() {
      return rdfa_xhtml_Format;
   }

   @Override
   public RDFParser getParser() {
      return new SesameRDFaParser.XHTMLRDFaParser();
   }

}
