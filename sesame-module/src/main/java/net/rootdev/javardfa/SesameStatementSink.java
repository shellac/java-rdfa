/*
 * (c) Copyright 2009 University of Bristol
 * All rights reserved.
 * [See end of file]
 */
package net.rootdev.javardfa;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Damian Steer <pldms@mac.com>
 * @author Henry Story <henry.story@bblfish.net>
 */
public class SesameStatementSink implements StatementSink {

   private static Logger log = LoggerFactory.getLogger(SesameStatementSink.class);
   private Map<String, Resource> bnodeLookup;
   ValueFactory valFactory;
   RDFHandler handler;

   SesameStatementSink(ValueFactory valFact, RDFHandler handler) {
      this.valFactory = valFact;
      this.handler = handler;
   }

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
         URI p = valFactory.createURI(predicate);
         Resource o = getResource(object);
         handler.handleStatement(valFactory.createStatement(s, p, o));
      } catch (RDFHandlerException ex) {
         java.util.logging.Logger.getLogger(SesameStatementSink.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   //@Override
   public void addLiteral(String subject, String predicate, String lex, String lang, String datatype) {
      try {
         Resource s = getResource(subject);
         URI p = valFactory.createURI(predicate);
         Literal o;
         if (lang == null && datatype == null) {
            o = valFactory.createLiteral(lex);
         } else if (lang != null) {
            o = valFactory.createLiteral(lex, lang);
         } else {
            URI dt = valFactory.createURI(datatype);
            o = valFactory.createLiteral(lex, dt);
         }
         handler.handleStatement(valFactory.createStatement(s, p, o));
      } catch (RDFHandlerException ex) {
         java.util.logging.Logger.getLogger(SesameStatementSink.class.getName()).log(Level.WARNING, null, ex);
      }
   }

   private Resource getResource(String res) {
      if (res.startsWith("_:")) {
         if (bnodeLookup.containsKey(res)) {
            return bnodeLookup.get(res);
         }
         Resource bnode = valFactory.createBNode();
         bnodeLookup.put(res, bnode);
         return bnode;
      } else {
         return valFactory.createURI(res);
      }
   }

   public void addPrefix(String prefix, String uri) {
       try {
         handler.handleNamespace(prefix, uri);
      } catch (RDFHandlerException rDFHandlerException) {
         java.util.logging.Logger.getLogger(SesameStatementSink.class.getName()).log(Level.WARNING, null, rDFHandlerException);
      }
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
