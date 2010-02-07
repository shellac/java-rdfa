/*
 * (c) 2009
 * Damian Steer <mailto:pldms@mac.com>
 */

package net.rootdev.javardfa;

import net.rootdev.javardfa.*;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import net.rootdev.javardfa.ParserFactory.Format;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author pldms
 */
public class Scratch {

    private static XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

    public static void main(String[] args) throws SAXException, IOException, ClassNotFoundException {
        String base = "http://rdfa.digitalbazaar.com/test-suite/test-cases/html5/";
        String testHTML = base + "0121.html";
        String testSPARQL = base + "0121.sparql";

        check(testHTML, testSPARQL, Format.HTML);

        //XMLReader parser = ParserFactory.createReaderForFormat(new NTripleSink(System.out), Format.HTML);
        //parser.parse(Scratch.class.getResource("/simple.html").toExternalForm());
        /*Class.forName(RDFaReader.class.getName());
        Model model = ModelFactory.createDefaultModel();
        //model.read("http://www.ivan-herman.net/foaf.html", "HTML");
        model.read("http://www.myspace.com/parishilton", "HTML");
        System.err.println("== Read ==");
        model.write(System.err, "TTL");*/
    }

    private static void check(String testHTML, String testSPARQL, Format format)
            throws SAXException, IOException {
        Model model = ModelFactory.createDefaultModel();
        StatementSink sink = new JenaStatementSink(model);
        InputStream in = FileManager.get().open(testHTML);

        XMLReader reader = ParserFactory.createReaderForFormat(sink, format);

        InputSource ins = new InputSource(in);
        ins.setEncoding("utf-8");
        ins.setSystemId(testHTML);
        reader.parse(ins);
        Query theQuery = QueryFactory.read(testSPARQL);
        QueryExecution qe = QueryExecutionFactory.create(theQuery, model);
        if (qe.execAsk()) {
            System.err.println("It worked! " + testHTML);
            return;
        }

        System.err.println("Failed: ");
        model.write(System.err, "TTL");
        System.err.println(theQuery);
    }

}
