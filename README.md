Welcome to java-rdfa
====================

The _cruftiest_ RDFa parser in the world, I'll bet. Apologies that there isn't much documentation. Things may explode: you have been warned.

Currently passing all [conformance](http://github.com/msporny/rdfa-test-suite) tests for XHTML, and the HTML 4 and 5 tests with [one exception](http://github.com/shellac/java-rdfa/issues#issue/15).

This was written by [Damian Steer](mailto:pldms@mac.com). It is an offshoot of the [Stars Project](http://stars.ilrt.bris.ac.uk/blog/) which was funded by [JISC](http://www.jisc.ac.uk/)

Useful Links
------------

* [Maven repository](http://www.rootdev.net/maven/repo/) ([snapshots](http://www.rootdev.net/maven/snapshot-repo/))
* [Java api documentation](http://www.rootdev.net/maven/projects/java-rdfa/apidocs/index.html)
* [Online parser](http://rdf-in-html.appspot.com/)

Basic Use
---------

	$ ls
	htmlparser-1.2.1.jar	java-rdfa-0.4.jar
	
	$ java -jar java-rdfa-0.4.jar http://examples.tobyinkster.co.uk/hcard
	<http://examples.tobyinkster.co.uk/hcard> <http://xmlns.com/foaf/0.1/primaryTopic> <http://examples.tobyinkster.co.uk/hcard#jack> .
	...

or (equivalent):
	
	$ java -cp '*' rdfa.simpleparse http://examples.tobyinkster.co.uk/hcard
	<http://examples.tobyinkster.co.uk/hcard> <http://xmlns.com/foaf/0.1/primaryTopic> <http://examples.tobyinkster.co.uk/hcard#jack> .
	...

For HTML sources add the format argument, and you will need the [validator.nu](http://about.validator.nu/htmlparser/) parser:
	    
	$ java -cp '*' rdfa.simpleparse --format HTML http://www.slideshare.net/intdiabetesfed/world-diabetes-day-2009
	<http://www.slideshare.net/intdiabetesfed/world-diabetes-day-2009> <http://www.w3.org/1999/xhtml/vocab#stylesheet> <http://public.slidesharecdn.com/v3/styles/combined.css?1265372095> .
	...

The output of simpleparse is n-triples, and hard to read. If you have [jena](http://openjena.org/) try adding it to you classpath and using rdfa.parse instead:

	$ java -cp '*:/path/to/jena/lib/*' rdfa.parse --format HTML http://www.slideshare.net/intdiabetesfed/world-diabetes-day-2009
	@prefix dc:      <http://purl.org/dc/terms/> .
	@prefix hx:      <http://purl.org/NET/hinclude> .
	... nice turtle output ...

Java Use
--------

To use the parser directly, without the assistance of an RDF toolkit (a bold choice) implement a [StatementSink](http://rootdev.net/maven/projects/java-rdfa/apidocs/net/rootdev/javardfa/StatementSink.html) to collect the triples, then use a parser from the [Factory](http://rootdev.net/maven/projects/java-rdfa/apidocs/net/rootdev/javardfa/ParserFactory.html) to make a reader:

	XMLReader reader = ParserFactory.createReaderForFormat(sink, Format.XHTML); // or HTML, still an XMLReader
	reader.parse(source); // Your sink will be sent triples

java-rdfa can be used from jena. Simply invoke:

	Class.forName("net.rootdev.javardfa.jena.RDFaReader");

Which will hook the two readers in to jena, then you will be able to:

	model.read(url, "XHTML"); // xml parsing
	model.read(other, "HTML"); // html parsing
	
java-rdfa is available in the maven central repositories. Note that it does not depend on jena.

A sesame reader provided by Henry Story is also available.

Open Graph Protocol
-------------------

A very simple OGP reader is provided. This follows what (I think) Toby Inkster did:

        Map<String, String> prop =
            OGPReader.getOGP("http://uk.rottentomatoes.com/m/1217700-kick_ass",
                             Format.HTML);

Result:

        title => 'Kick-Ass'
        http://www.facebook.com/2008/fbml#app_id => '326803741017'
        http://www.w3.org/1999/xhtml/vocab#icon => 'http://images.rottentomatoes.com/images/icons/favicon.ico'
        http://www.w3.org/1999/xhtml/vocab#stylesheet => 'http://images.rottentomatoes.com/files/inc_beta/generated/css/mob.css'
        image => 'http://images.rottentomatoes.com/images/movie/custom/00/1217700.jpg'
        site_name => 'Rotten Tomatoes'
        type => 'movie'
        url => 'http://www.rottentomatoes.com/m/1217700-kick_ass/'
        http://www.facebook.com/2008/fbml#admins => '1106591'

Form Mode
---------

There is a secret form mode (that prompted the development of this parser). In this mode you can generate basic graph patterns by including ?variables where curies are allowed, and INPUT tags generate @name variables.

[Simple example](http://github.com/shellac/java-rdfa/tree/master/src/test/resources/query-tests/1.html)
(from the tests) and the [query](http://github.com/shellac/java-rdfa/tree/master/src/test/resources/query-tests/1.rq) that results.

Changes
-------

### 0.4 ###

* (Finally) support overlapping literals. No one noticed this didn't work!
* Added turtle-ish output. Slightly less nasty than N-Triples.
* Bug fixes...
* Turned OFF html 5 streaming. Such a bad idea on my part.
* Started RDFa 1.1 support.
* Added simple OGP reader.

### 0.3 ###

* Updated to current conformance tests
* Switched validator.nu to streaming mode (may live to regret this).
* Created very simple n-triple and rdf/xml streaming serialisers.
* Usual bug fixes etc.
* Jena is now a provided maven dependency. Using java-rdfa won't pull in jena.
* Sesame reader create by Henry Story added. Can't be added to central maven repository since Sesame isn't available, so spun out in small module.
* Tests for query, and some utilities.
