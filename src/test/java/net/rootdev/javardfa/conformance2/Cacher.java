package net.rootdev.javardfa.conformance2;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RIOT;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.LocationMapper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A utility to retrieve and store the conformance tests locally.
 * Uses jena's location mapping
 * 
 * @author Damian Steer <d.steer@bris.ac.uk>
 */
public class Cacher {
    
    final static Logger log = LoggerFactory.getLogger(Cacher.class);
    
    final static String CACHED = "//////CACHED/////";
    
    public static void main(String... args) {
        RIOT.init();
        Cacher c = new Cacher("src/test/resources/", false);
        c.cacheManifest("http://rdfa.info/test-suite/rdfa1.0/svg/manifest");
        c.cacheManifest("http://rdfa.info/test-suite/rdfa1.0/xhtml/manifest");
        c.cacheManifest("http://rdfa.info/test-suite/rdfa1.0/xml/manifest");
        c.cacheManifest("http://rdfa.info/test-suite/rdfa1.1/xml/manifest");
    }
    
    private final LocationMapper lm;
    private final String base;
    private final boolean forceDownload;
    
    public Cacher(String base, boolean forceDownload) {
        this.lm = LocationMapper.get();
        this.base = base;
        this.forceDownload = forceDownload;
    }
    
    public void cacheManifest(String uri) {
        String location = cache(uri);
        
        if (location == null) {
            log.error("Failed to get manifest <{}>", uri);
            return;
        }
        
        try {
            Model manifest = FileManager.get().loadModel(uri, "TTL");
            
            NodeIterator ni = manifest.listObjects();
            // Download every object (bit broad?!)
            while (ni.hasNext()) {
                RDFNode n = ni.next();
                if (n.isURIResource()) {
                    String res = cache(n.asResource().getURI());
                    if (res == null) System.out.print("!");
                    else if (res == CACHED) System.out.print("-");
                    else System.out.print("+");
                }
            }
            System.out.println();
        } catch (Exception e) {
            log.error("Issue caching manifest <{}>: {}", uri, e);
        }
    }
    
    public String cache(String uri) {
        try {
            return cacheInternal(uri);
        } catch (Exception ex) {
            log.error("Error getting <{}>: {}", uri, ex);
        }
        return null;
    }
    
    private String cacheInternal(String uri) throws MalformedURLException, IOException {
        String location = lm.altMapping(uri);
        
        if (location == null || uri.equals(location)) {
            log.error("No mapping for <{}>", uri);
            return null;
        }
        
        location = location.replace("file:", "");
        
        log.info("Location is: {}\n", location);
        
        File target = new File(base, location);
        if (!forceDownload && target.isFile()) {
            log.info("<{}> already stored.", uri);
            return CACHED;
        }
        if (target.getParentFile().exists() && !target.getParentFile().isDirectory()) {
            log.error("<{}> is not a directory", target.getParentFile());
            return null;
        }
        if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
            log.error("Couldn't create <{}>", target.getParentFile());
            return null;
        }
        
        HttpURLConnection conn = null;
        BufferedInputStream in = null;
        FileOutputStream out = null;
        try {
            URL url = new URL(uri);
            conn = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(conn.getInputStream());
            if (conn.getResponseCode() / 100 != 2) {
                log.error("<{}> returned code {}", uri, conn.getResponseCode());
                return null;
            }
            out = new FileOutputStream(target);
            byte[] buffer = new byte[2048];
            int got;
            while ((got = in.read(buffer)) != -1) {
                out.write(buffer, 0, got);
            }
            return location;
        } finally {
            if (out != null) out.close();
            if (in != null) in.close();
            if (conn.getErrorStream() != null) conn.getErrorStream().close();
        }
    }
    
}
