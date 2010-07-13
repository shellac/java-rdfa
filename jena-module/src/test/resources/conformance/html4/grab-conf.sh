rm *.xhtml
rm *.sparql
rm *.html

MANIFEST='http://github.com/msporny/rdfa-test-suite/raw/master/html4-manifest.rdf'

curl -S -s -O "$MANIFEST"

MANIFEST_FILE=$(basename "$MANIFEST")

for i in $(grep 'informationResourceInput' $MANIFEST_FILE | cut -d '"' -f 2)
do
	curl -S -s -O "$i"
done

for i in $(grep 'informationResourceResults' $MANIFEST_FILE | cut -d '"' -f 2)
do
	curl -S -s -O "$i"
done
