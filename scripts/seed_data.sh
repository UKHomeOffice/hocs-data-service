#!/usr/bin/env bash

target_uri=${1}
data_dir=${2}

# Wait for the hocs data service to become available
until curl -s $target_uri/healthz
do
    echo "Waiting for hocs-data-service to come up"
    sleep 2
done

# Begin POSTs to the service to seed data
echo "hocs-data-service is up! Seeding data"

sleep 2

echo "Posting DCU Topics"
curl -vX POST $target_uri/topics/DCU -F "file=@$data_dir/DCU_Topics.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting FOI Topics"
curl -vX POST $target_uri/topics/FOI -F "file=@$data_dir/DCU_Topics.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting UKVI Topics"
curl -vX POST $target_uri/topics/UKVI -F "file=@$data_dir/UKVI_Topics.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting Unit and Team structures"
curl -vX POST $target_uri/groups -F "file=@$data_dir/Unit_Team_Structure.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting DCU Users"
curl -vX POST $target_uri/users/DCU -F "file=@$data_dir/DCU_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting FOI Users"
curl -vX POST $target_uri/users/FOI -F "file=@$data_dir/FOI_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting HMPO CCC Users"
curl -vX POST $target_uri/users/HMPOCCC -F "file=@$data_dir/HMPO_CCC_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting HMPO COL Users"
curl -vX POST $target_uri/users/HMPOCOL -F "file=@$data_dir/HMPO_Collectives_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting UKVI Users"
curl -vX POST $target_uri/users/UKVI -F "file=@$data_dir/UKVI_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting Test Users"
curl -vX POST $target_uri/users/TEST_USERS -F "file=@$data_dir/Test_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting Minister List"
curl -vX POST $target_uri/list/ -d "@$data_dir/Minister_List.JSON" \
 -H "Content-Type: application/json"

echo "Pulling member information from external API"
curl -v -o /dev/null -v $target_uri/list/api/refresh

echo "Posting Welsh Assembly List"
curl -vX POST $target_uri/list/ -d "@$data_dir/welsh_assembly_list.json" \
 -H "Content-Type: application/json"

# Perform a series of GETs to prime the cache for the seeded data
echo "Priming cache on resources"

sleep 2

curl -v -o /dev/null -v $target_uri/topics/topicList

curl -v -o /dev/null -v $target_uri/list/MinisterList

curl -v -o /dev/null -v $target_uri/groups

