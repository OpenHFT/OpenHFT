#! /bin/bash

#Fail on any error
set -e

DIR=${PWD##*/}
VERSION=$(mvn -Dexec.executable='echo' -Dexec.args='${project.version}' --non-recursive exec:exec -q)
BRANCH=$(git branch | awk '/\*/ { print $2; }')

printf "%-36s %-22s %s\n" $DIR $BRANCH $VERSION
