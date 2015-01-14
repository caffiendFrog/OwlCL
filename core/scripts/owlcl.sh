#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  OWLCL_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$OWLCL_DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
OWLCL_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

echo OwlCL directory is: ${OWLCL_DIR}

JAVA=$(which java)

if [[ $OWLCL_JAVA ]]; then
	JAVA=${OWLCL_JAVA}
fi

echo OwlCL Java is: ${JAVA}

OWLCL_OPTS="-Xmx1024m"

"${JAVA}" ${OWLCL_OPTS} -jar "${OWLCL_DIR}"/owlcl.jar  "$@"
