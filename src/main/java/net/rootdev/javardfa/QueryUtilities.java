/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import com.hp.hpl.jena.query.Query;
import java.io.IOException;
import java.util.Map;
import net.rootdev.javardfa.ParserFactory.Format;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 *
 * Some useful functions concerning pages with variables
 *
 * @author pldms
 */
public class QueryUtilities {

    /**
     * Grab simple (BGP) queries from an html document. Over named graphs currently.
     *
     * @param format document format
     * @param source document id
     * @return
     */
    public static Map<String, Query> makeQueries(Format format, String source) throws SAXException, IOException {
        QueryCollector qc = new QueryCollector();
        XMLReader reader = ParserFactory.createReaderForFormat(qc, format);
        ((Parser) reader.getContentHandler()).enable(Setting.FormMode);
        reader.parse(source);
        return qc.getQueries();
    }

}
