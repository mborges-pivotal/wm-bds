#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters. Need directory and port"
    exit 1
fi

export executable="gpfdist -v -d $1 -p $2"
export log=gpfdist.log

{ sh -c "$executable > $log 2>&1 &"'
echo $! > pidfile
echo   # Alert parent that the pidfile has been written
wait $!
echo $? > exit-status
' & } | read
