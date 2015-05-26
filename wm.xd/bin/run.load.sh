#!/bin/bash

FP=/home/gpadmin/wm-bds/wm.util/target/load.sql
#batch_id=1
#load_date=20150505

#batch_id=2
#load_date=20150519

batch_id=3
load_date=20150521

echo "+---------------------------------------+------------+"
echo "| Running load script for batch_id $batch_id and load_date $load_date"
echo "+---------------------------+-----------+------------+"

# result=`psql --field-separator ':' -Atc "select count(*),pg_size_pretty(pg_relation_size('"$table"')) from "$view";"`
result=`psql -d gpadmin -w -v batch_id=$batch_id -v load_date=\'$load_date\' -f $FP`;
echo "result: $result"

echo "+---------------------------+-----------+------------+"
exit 

