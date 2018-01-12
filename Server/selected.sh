#!/bin/bash

# create test event
echo "== "
echo "== Retrieving selected video"
echo "== "
curl -i --request GET http://127.0.0.1:1234/selected
echo ""

#echo ">> Uploading file"
#curl -i --request PUT --data-binary "@input.pdf" http://127.0.0.1:1234/video/1
#curl -v -include --form file=input.pdf --form upload=@input.pdf http://127.0.0.1:1234/upload
#echo ">> Downloading file"
#wget http://127.0.0.1:1234/video/1 -O download.pdf

exit 0
