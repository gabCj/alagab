#!/bin/bash

pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

cat << EndOfMessage
HELP: 
./repertoireNoms.sh ip_address
	- ip_address: (OPTIONAL) L'addresse ip du serveur.
	  Si l'arguement est non fourni, on conisdère que le serveur est local (ip_address = 127.0.0.1)

EndOfMessage

IPADDR=$1
if [ -z "$1" ]
  then
    IPADDR="127.0.0.1"
fi

java -cp "$basepath"/repertoireNoms.jar:"$basepath"/shared.jar \
  -Djava.rmi.server.codebase=file:"$basepath"/shared.jar \
  -Djava.security.policy="$basepath"/policy \
  -Djava.rmi.server.hostname="$IPADDR" \
  repertoireNoms.RepertoireNoms