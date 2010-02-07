/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rootdev.javardfa;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author hjs
 */
public class SesameUtils {

   public static String getContentAsString(URL url) throws IOException {
      URLConnection conn = url.openConnection();
      conn.connect();
      InputStream in = conn.getInputStream();
      Reader r = new InputStreamReader(in, "UTF-8");
      int c;
      StringBuffer buf = new StringBuffer();
      while ((c = r.read()) != -1) {
         buf.append((char) c);
      }
      return buf.toString();
   }

   public static RepositoryConnection fetchResource(URL actualUrl, RDFFormat rdfFormat) throws SailException, RepositoryException, RDFParseException, IOException, MalformedURLException {
      URL base = new URL(actualUrl.getProtocol(), actualUrl.getHost(), actualUrl.getPort(), actualUrl.getFile()); // all of this needs
      MemoryStore mem = new MemoryStore();
      mem.initialize();
      SailRepository sail = new SailRepository(mem);
      RepositoryConnection rep = sail.getConnection();
      ValueFactory vf = sail.getValueFactory();
      // to be better
      org.openrdf.model.URI foafdocUri = vf.createURI(base.toString());
      HttpURLConnection conn = (HttpURLConnection) base.openConnection();
      conn.addRequestProperty("Accept:", rdfFormat.getDefaultMIMEType());
      conn.connect();
      InputStream foafin = conn.getInputStream();
      rep.add(foafin, actualUrl.toString(), rdfFormat, foafdocUri);
      return rep;
   }
}
