rm *.xhtml
rm *.sparql

MANIFEST='http://github.com/msporny/rdfa-test-suite/raw/master/xhtml-manifest.rdf'

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
