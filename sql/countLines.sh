#!/bin/bash

FP=/home/gpadmin/wm-bds/wm/data/pivotal_files_050515

echo ""
echo ""

if [ "$#" -eq 1 ]; then
  FP=$1
  echo "Parameters: $#"
fi

echo ""
echo " Folder with CSV files"
echo " $FP"
echo ""
echo "+---------------------------------------+"
echo "| Files                     |   Lines   |"
echo "+---------------------------+-----------+"
while read f; do
  wc -l $FP/$f* | awk 'END{printf "| %-25s | %7d\t|\n",f,$1}' f="$f"
done < tables.txt
echo "+---------------------------+-----------+"
