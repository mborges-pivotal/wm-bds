#!/bin/bash

if [ "$#" -ne 4 ]; then
    echo "Illegal number of parameters. Need directory, port, batch_id and load_date (YYYYMMDD)"
    exit 1
fi

# ./start-gpfdist.sh /home/gpadmin/wm-bds/wm/data/pivotal_files_052115 7171
export executable="gpfdist -v -d $1 -p $2"
export log=gpfdist.log

{ sh -c "$executable > $log 2>&1 &"'
echo $! > pidfile
echo   # Alert parent that the pidfile has been written
wait $!
echo $? > exit-status
' & } | read

echo "gpfdist started with $1 and $2 ..."
sleep 5

FP=/home/gpadmin/wm-bds/wm.util/target/load.sql

batch_id=$3
load_date=$4

echo "Running batch load with $batch_id and $load_date..."
result=`psql -d gpadmin -w -v batch_id=$batch_id -v load_date=\'$load_date\' -f $FP`;
echo "result: $result"


echo "Stopping gpfidst..."
kill -9 $(cat pidfile)
rm pidfile
rm gpfdist.log
rm exit-status
