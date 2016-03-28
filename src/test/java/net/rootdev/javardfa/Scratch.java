/*
 * (c) 2009
 * Damian Steer <mailto:pldms@mac.com>
 */

package net.rootdev.javardfa;

import net.rootdev.javardfa.jena.JenaStatementSink;
import net.rootdev.javardfa.*;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.stream.XMLInputFactory;
import net.rootdev.javardfa.ParserFactory.Format;
import net.rootdev.javardfa.output.OGPReader;
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
        Map<String, String> prop = OGPReader.getOGP("http://uk.rottentomatoes.com/m/1217700-kick_ass", Format.HTML);

        for (Entry<String, String> ent: prop.entrySet()) {
            System.err.printf("[%s] => '%s'\n", ent.getKey(), ent.getValue());
        }

        if (true) return;

        String base = "http://rdfa.digitalbazaar.com/test-suite/test-cases/xhtml1/";
        String testHTML = base + "0121.xhtml";
        String testSPARQL = base + "0121.sparql";

        check(testHTML, testSPARQL, Format.XHTML);

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
