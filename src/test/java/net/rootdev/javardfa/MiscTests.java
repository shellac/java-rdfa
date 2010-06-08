/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rootdev.javardfa;

import net.rootdev.javardfa.uri.URIResolver;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pldms
 */
public class MiscTests {

    @Test
    public void CheckURIResolver() {
        Resolver resolver = new URIResolver();
        String url = "javascript:working_ajax('div_trailers_row_ID_0', '/trailers_pictures/photo_strip_ajax.php?media_type=trailers&skin=mob&type=2&id=1144763');";
        try {
            String resolved = resolver.resolve("http://example.com/", url);
            assertNull(resolved);
        } catch (Exception e) {
            e.printStackTrace();
            fail("resolver threw exception: " + e.getMessage());
        }
    }

}
