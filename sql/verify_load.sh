#!/bin/bash

v_suffix="_view"
a_suffix="_ao"

echo "+---------------------------------------+------------+"
echo "| Table Name                |   Count   | Size       |"
echo "+---------------------------+-----------+------------+"

while read f; do
   declare table="$f$a_suffix"
   declare view="$f$v_suffix"
   result=`psql --field-separator ':' -Atc "select count(*),pg_size_pretty(pg_relation_size('"$table"')) from "$view";"`
   echo "$result" | awk -F":" '{printf "| %-25s | %7s\t| %-10s |\n",f,$1,$2}' f="$f"
done <tables.txt

echo "+---------------------------+-----------+------------+"
exit 

