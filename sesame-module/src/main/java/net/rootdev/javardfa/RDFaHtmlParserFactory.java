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
public class RDFaHtmlParserFactory implements RDFParserFactory {

   public static final RDFFormat rdfa_html_Format;

   static {
      rdfa_html_Format = new RDFFormat("rdfa-html", "text/html",
              Charset.forName("UTF-8"), "html", true, false);
      RDFFormat.register(rdfa_html_Format);
   }

   @Override
   public RDFFormat getRDFFormat() {
      return rdfa_html_Format;
   }

   @Override
   public RDFParser getParser() {
      return new SesameRDFaParser.HTMLRDFaParser();
   }
}
