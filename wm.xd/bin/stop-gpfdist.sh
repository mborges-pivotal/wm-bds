#!/bin/bash
kill -9 $(cat pidfile)
rm pidfile
rm gpfdist.log
rm exit-status
