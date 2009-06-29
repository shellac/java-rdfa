rm *.xhtml
rm *.sparql

MANIFEST='http://www.w3.org/2006/07/SWD/RDFa/testsuite/xhtml1-testcases/rdfa-xhtml1-test-manifest.rdf'

curl -S -s -O "$MANIFEST"

echo $(basename $MANIFEST) $MANIFEST > mapfile

for i in $(grep 'informationResourceInput' rdfa-xhtml1-test-manifest.rdf | cut -d '"' -f 2)
do
	curl -S -s -O "$i"
	echo $(basename $i) $i >> mapfile
done

for i in $(grep 'informationResourceResults' rdfa-xhtml1-test-manifest.rdf | cut -d '"' -f 2)
do
	curl -S -s -O "$i"
	echo $(basename $i) $i >> mapfile
done