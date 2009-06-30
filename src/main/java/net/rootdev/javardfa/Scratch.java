/*
 * (c) 2009
 * Damian Steer <mailto:pldms@mac.com>
 */

package net.rootdev.javardfa;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author pldms
 */
public class Scratch {

    private static XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

    public static void main(String[] args) throws XMLStreamException, IOException, URISyntaxException {
        xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        String base = "http://www.w3.org/2006/07/SWD/RDFa/testsuite/xhtml1-testcases/";
        String testHTML = base + "0116.xhtml";
        String testSPARQL = base + "0116.sparql";

        check(testHTML, testSPARQL);
    }

    private static void check(String testHTML, String testSPARQL) throws XMLStreamException, IOException, URISyntaxException {
        Model model = ModelFactory.createDefaultModel();
        StatementSink sink = new JenaStatementSink(model);
        InputStream in = FileManager.get().open(testHTML);
        XMLEventReader reader = xmlFactory.createXMLEventReader(in);
        Parser parser = new Parser(reader, sink);
        parser.parse(testHTML);
        Query theQuery = QueryFactory.read(testSPARQL);
        QueryExecution qe = QueryExecutionFactory.create(theQuery, model);
        if (!qe.execAsk()) {
            System.err.println("It worked! " + testHTML);
            return;
        }

        System.err.println("Failed: ");
        model.write(System.err, "TTL");
    }

}
