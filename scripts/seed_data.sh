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
curl -sX POST http://${hostname}/legacy/topic/DCU -F "file=@/app/data/DCU_Topics.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting UKVI Topics"
curl -sX POST http://${hostname}/legacy/topic/UKVI -F "file=@/app/data/UKVI_Topics.csv" \
 -H "Content-Type: multipart/form-data"