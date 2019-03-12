#!/bin/bash

pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

cat << EndOfMessage
HELP: 
./repartiteur.sh ip_address
	- ip_address: (OPTIONAL) L'addresse ip du serveur.
	  Si l'arguement est non fourni, on conisdère que le serveur est local (ip_address = 127.0.0.1)

EndOfMessage

java -cp "$basepath"/repartiteur.jar:"$basepath"/shared.jar -Djava.security.policy="$basepath"/policy repartiteur.Repartiteur $1
