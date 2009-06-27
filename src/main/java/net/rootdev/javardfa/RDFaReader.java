/*
 * (c) 2009
 * Damian Steer <mailto:pldms@mac.com>
 */

package net.rootdev.javardfa;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFReader;
import java.io.InputStream;
import java.io.Reader;

/**
 *
 * @author pldms
 */
public class RDFaReader implements RDFReader
{

    public void read(Model arg0, Reader arg1, String arg2)
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    public void read(Model arg0, InputStream arg1, String arg2)
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    public void read(Model arg0, String arg1)
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    public Object setProperty(String arg0, Object arg1)
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

    public RDFErrorHandler setErrorHandler(RDFErrorHandler arg0)
      {
        throw new UnsupportedOperationException("Not supported yet.");
      }

}
