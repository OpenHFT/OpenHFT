#! /bin/bash

#Fail on any error
set -e

printf "%-36s %-22s %s\n" Library Branch Version
find . -type d -name .git -execdir sh -c "../status.sh" \;

