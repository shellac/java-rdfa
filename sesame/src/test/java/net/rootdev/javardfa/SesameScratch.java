/*
 * (c) 2009
 * Damian Steer <mailto:pldms@mac.com>
 */
package net.rootdev.javardfa;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.SailException;
import org.xml.sax.SAXException;

/**
 *
 * @author pldms
 */
public class SesameScratch {

//   private static XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

   public static void main(String[] args) throws MalformedURLException, SailException, RepositoryException, IOException, RDFParseException, RDFHandlerException, ClassNotFoundException, SAXException, MalformedQueryException, QueryEvaluationException {
      //xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
      String base = "http://www.w3.org/2006/07/SWD/RDFa/testsuite/xhtml1-testcases/";
      String testHTML = base + "0101.xhtml";
      String testSPARQL = base + "0101.sparql";

      Class.forName(RDFaXHtmlParserFactory.class.getName());
      Class.forName(RDFaHtmlParserFactory.class.getName());
      RepositoryConnection rep = test(testHTML, "application/xhtml+xml");
//      RepositoryConnection rep = test("http://nicecupoftea.org/", "text/html");
      check(rep, testSPARQL);

      //      test("http://www.w3.org/2006/07/SWD/RDFa/testsuite/xhtml1-testcases/0006.xhtml","application/xhtml+xml");

//      actualUrl = new URL("http://www.w3.org/2006/07/SWD/RDFa/testsuite/xhtml1-testcases/0006.xhtml");
//      RDFFormat rdfXhtml = RDFFormat.forMIMEType("application/xhtml+xml");
//       rep = fetchResource(actualUrl, rdfXhtml);
//
//      System.err.println("== Read ==");
//      rep.export(new TurtleWriter(System.out));


   }


 
   private static void check(RepositoryConnection conn, String testSPARQL) throws SAXException, IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
//        Model model = ModelFactory.createDefaultModel();
//        StatementSink sink = new JenaStatementSink(model);
//        InputStream in = FileManager.get().open(testHTML);
//        Parser parser = new Parser(sink);
//        parser.setBase(testHTML);
//        XMLReader reader = XMLReaderFactory.createXMLReader();
//        reader.setContentHandler(parser);
//        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
//        reader.parse(new InputSource(in));
         
      String query = SesameUtils.getContentAsString(new URL(testSPARQL));
      System.out.println("----QUERY----");
      System.out.println(query);
      System.out.println("---end query----");
      BooleanQuery q = conn.prepareBooleanQuery(QueryLanguage.SPARQL, query);
      if (q.evaluate()) {
         System.out.println("GOOD");
      } else {
         System.out.println("BAD");
      };
//        Query theQuery = QueryFactory.read(testSPARQL);
//        QueryExecution qe = QueryExecutionFactory.create(theQuery, model);
//        if (qe.execAsk()) {
//            System.err.println("It worked! " + testHTML);
//            return;
//        }
//
//        System.err.println("Failed: ");
//        model.write(System.err, "TTL");
//        System.err.println(theQuery);
   }


   private static RepositoryConnection test(String url, String mime) throws IOException, RDFParseException, RDFHandlerException, MalformedURLException, SailException, RepositoryException {
//        Class.forName(RDFaReader.class.getName());
//        Model model = ModelFactory.createDefaultModel();
//        model.read("http://www.ivan-herman.net/foaf.html", "HTML");
//        System.err.println("== Read ==");
//        model.write(System.err, "TTL");
//      URL actualUrl = new URL("http://www.ivan-herman.net/foaf.html");
      System.err.println("== Fetching " +url +" with mime type "+mime+"==");
      URL actualUrl = new URL(url);
      RDFFormat rdfhtml = RDFFormat.forMIMEType(mime);
      RepositoryConnection rep = SesameUtils.fetchResource(actualUrl, rdfhtml);
      System.err.println("== Read ==");
      rep.export(new TurtleWriter(System.out));
      System.err.println("====DONE======");
      System.err.println("");
      return rep;
   }
}
