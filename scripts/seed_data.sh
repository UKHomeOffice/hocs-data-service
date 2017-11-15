#!/usr/bin/env bash

# Wait for the hocs data service to become available
until curl -s http://${hostname}/healthz
do
    echo "Waiting for hocs-data-service to come up"
    sleep 5
done

# Begin POSTs to the service to seed data
echo "hocs-data-service is up! Seeding data"

sleep 5

echo "Posting DCU Topics"
curl -sX POST http://${hostname}/topics/DCU -F "file=@/app/data/DCU_Topics.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting UKVI Topics"
curl -sX POST http://${hostname}/topics/UKVI -F "file=@/app/data/UKVI_Topics.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting Unit and Team structures"
curl -sX POST http://${hostname}/groups -F "file=@/app/data/Unit_Team_Structure.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting DCU Users"
curl -sX POST http://${hostname}/users/DCU -F "file=@/app/data/DCU_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting FOI Users"
curl -sX POST http://${hostname}/users/FOI -F "file=@/app/data/FOI_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting HMPO CCC Users"
curl -sX POST http://${hostname}/users/HMPOCCC -F "file=@/app/data/HMPO_CCC_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting HMPO COL Users"
curl -sX POST http://${hostname}/users/HMPOCOL -F "file=@/app/data/HMPO_Collectives_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting UKVI Users"
curl -sX POST http://${hostname}/users/UKVI -F "file=@/app/data/UKVI_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting Minister List"
curl -sX POST http://${hostname}/list/ -d "@/app/data/Minister_List.JSON" \
 -H "Content-Type: application/json"

echo "Posting Test Users"
curl -sX POST http://${hostname}/list/ -d "@/app/data/Test_Users.JSON" \
 -H "Content-Type: application/json"

# Perform a series of GETs to prime the cache for the seeded data
echo "Priming cache on resources"

sleep 5

curl -s -o /dev/null -v http://${hostname}/topics/topicList

curl -s -o /dev/null -v http://${hostname}/list/MinisterList

curl -s -o /dev/null -v http://${hostname}/groups
