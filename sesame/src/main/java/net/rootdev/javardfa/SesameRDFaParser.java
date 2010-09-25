/*
New BSD license: http://opensource.org/licenses/bsd-license.php

Copyright (c) 2009 Sun Microsystems, Inc.
901 San Antonio Road, Palo Alto, CA 94303 USA.
All rights reserved.


Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

- Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.
- Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.
- Neither the name of Sun Microsystems, Inc. nor the names of its contributors
may be used to endorse or promote products derived from this software
without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
 */
package net.rootdev.javardfa;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFParserBase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * RDFa parser for HTML5 and XHTML (see inner classes).
 * 
 * @author Henry Story <henry.story@bblfish.net>
 * @author Lars Heuer <heuer[at]semagia.com>
 */
public abstract class SesameRDFaParser extends RDFParserBase {

   private static Logger log = LoggerFactory.getLogger(SesameRDFaParser.class);

   private XMLReader xmlReader;

   public static class HTMLRDFaParser extends SesameRDFaParser {

      @Override
      public XMLReader getReader() {
         HtmlParser reader = new HtmlParser();
         reader.setXmlPolicy(XmlViolationPolicy.ALLOW);
         reader.setXmlnsPolicy(XmlViolationPolicy.ALLOW);
         reader.setMappingLangToXmlLang(false);
         return reader;
      }

      @Override
      public void initParser(Parser parser) {
         parser.enable(Setting.ManualNamespaces);
      }

      @Override
      public RDFFormat getRDFFormat() {
         return RDFaHtmlParserFactory.rdfa_html_Format;
      }
   }

   public static class XHTMLRDFaParser extends SesameRDFaParser {

      @Override
      public XMLReader getReader() throws SAXException {
         XMLReader reader = XMLReaderFactory.createXMLReader();
         reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
         return reader;
      }

      public RDFFormat getRDFFormat() {
         return RDFaXHtmlParserFactory.rdfa_xhtml_Format;
      }
   }

   public void setReader(XMLReader reader) {
      this.xmlReader = reader;
   }

   protected XMLReader getReader() throws SAXException {
      return xmlReader;
   }

   protected void initParser(Parser parser) {
   }

   public void parse(InputStream in, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
      parse(new InputSource(in), baseURI);
   }

   public void parse(Reader reader, String baseURI) throws IOException, RDFParseException, RDFHandlerException {
      parse(new InputSource(reader), baseURI);
   }

   private void parse(InputSource in, String baseURI) throws IOException {
      Parser parser = new Parser(new SesameStatementSink());
      parser.setBase(baseURI);
      initParser(parser);
      try {
         XMLReader xreader = getReader();
         xreader.setContentHandler(parser);
         xreader.parse(in);
      } catch (SAXException ex) {
         throw new RuntimeException("SAX Error when parsing", ex);
      }
   }


   private class SesameStatementSink implements StatementSink {

       private Map<String, Resource> bnodeLookup;


       //@Override
       public void start() {
          bnodeLookup = new HashMap<String, Resource>();
       }

       //@Override
       public void end() {
          bnodeLookup = null;
       }

       //@Override
       public void addObject(String subject, String predicate, String object) {
          try {
             Resource s = getResource(subject);
             Resource o = getResource(object);
             rdfHandler.handleStatement(createStatement(s, createURI(predicate), o));
          } catch (OpenRDFException ex) {
              log.warn("Unexpected exception", ex);
          }
       }

       @Override
       public void addLiteral(String subject, String predicate, String lex, String lang, String datatype) {
          try {
             Resource s = getResource(subject);
             URI p = createURI(predicate);
             Literal o = createLiteral(lex, lang, datatype != null ? createURI(datatype) : null);
             rdfHandler.handleStatement(createStatement(s, p, o));
          } catch (OpenRDFException ex) {
              log.warn("Unexpected exception", ex);
          }
       }

       private Resource getResource(String res) throws RDFParseException {
          if (res.startsWith("_:")) {
             if (bnodeLookup.containsKey(res)) {
                return bnodeLookup.get(res);
             }
             Resource bnode = createBNode();
             bnodeLookup.put(res, bnode);
             return bnode;
          } else {
             return createURI(res);
          }
       }

       @Override
       public void addPrefix(String prefix, String uri) {
           try {
             rdfHandler.handleNamespace(prefix, uri);
          } catch (RDFHandlerException ex) {
              log.warn("Unexpected exception", ex);
          }
       }

       @Override
       public void setBase(String base) {
           // TODO Auto-generated method stub
       }

    }
}