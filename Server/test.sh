#!/bin/bash

# create test event
echo "== "
echo "== Registering event"
echo "== "
curl -H "Content-Type: application/json" -d '{"name":"test event"}' http://localhost:1234/event/new
echo ""

echo "== "
echo "== Retrieving all events"
echo "== "
curl -i --request GET http://127.0.0.1:1234/events
echo ""

# get default event
echo "== "
echo "== Retrieving event"
echo "== "
curl -i --request GET http://127.0.0.1:1234/event/111
echo ""

# create videos for events
echo "== "
echo "== Registering video for non-existing event"
echo "== "
curl -H "Content-Type: application/json" -d '{"name":"test video"}' http://localhost:1234/event/9001
echo ""

echo "== "
echo "== Registering video for existing event"
echo "== "
curl -H "Content-Type: application/json" -d '{"name":"Awesome video"}' http://localhost:1234/event/111
curl -H "Content-Type: application/json" -d '{"name":"Even better video"}' http://localhost:1234/event/111
echo ""

echo "== "
echo "== Retrieving event (with videos)"
echo "== "
curl -i --request GET http://127.0.0.1:1234/event/111
echo ""

echo "== "
echo "== Retrieving all events"
echo "== "
curl -i --request GET http://127.0.0.1:1234/events
echo ""

echo ">> Uploading file"
curl -i --request PUT --data-binary "@input.pdf" http://127.0.0.1:1234/video/111
#curl -v -include --form file=input.pdf --form upload=@input.pdf http://127.0.0.1:1234/upload
echo ">> Downloading file"
wget http://127.0.0.1:1234/video/111 -O download.pdf

echo "== "
echo "== Retrieving /selected"
echo "== "
curl -i --request GET http://127.0.0.1:1234/selected
echo ""

exit 0
