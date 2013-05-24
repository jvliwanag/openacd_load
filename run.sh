JAVA=java

function join() {
    local IFS=$1
    shift
    echo "$*"
}

jars=lib/*.jar
jarscp=$(join ':' $jars)

if [ ! -e "config.properties" ]; then
	echo Creating config.properties from example
	cp config.properties.example config.properties
fi

echo Running...
$JAVA -classpath $jarscp:bin Main1
