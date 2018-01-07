#!/usr/bin/env bash

target_uri=${1}
data_dir=${2}
user=${3}
pass=${4}

# Wait for the hocs data service to become available
until curl $user:$pass -s $target_uri/healthz
do
    echo "Waiting for hocs-data-service to come up"
    sleep 2
done

# Begin POSTs to the service to seed data
echo "hocs-data-service is up! Seeding data"

sleep 2

echo "Posting DCU Topics"
curl -u $user:$pass -X POST $target_uri/topics/DCU -F "file=@$data_dir/DCU_Topics.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting FOI Topics"
curl -u $user:$pass -X POST $target_uri/topics/FOI -F "file=@$data_dir/DCU_Topics.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting UKVI Topics"
curl -u $user:$pass -X POST $target_uri/topics/UKVI -F "file=@$data_dir/UKVI_Topics.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting Unit and Team structures"
curl -u $user:$pass -X POST $target_uri/units -F "file=@$data_dir/Unit_Team_Structure.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting DCU Users"
curl -u $user:$pass -X POST $target_uri/users/dept/DCU -F "file=@$data_dir/DCU_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting FOI Users"
curl -u $user:$pass -X POST $target_uri/users/dept/FOI -F "file=@$data_dir/FOI_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting HMPO CCC Users"
curl -u $user:$pass -X POST $target_uri/users/dept/HMPOCCC -F "file=@$data_dir/HMPO_CCC_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting HMPO COL Users"
curl -u $user:$pass -X POST $target_uri/users/dept/HMPOCOL -F "file=@$data_dir/HMPO_Collectives_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting UKVI Users"
curl -u $user:$pass -X POST $target_uri/users/dept/UKVI -F "file=@$data_dir/UKVI_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting Test Users"
curl -u $user:$pass -X POST $target_uri/users/dept/TEST_USERS -F "file=@$data_dir/Test_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting Minister List"
curl -u $user:$pass -X POST $target_uri/list/ -d "@$data_dir/Minister_List.JSON" \
 -H "Content-Type: application/json"

echo "Pulling member information from external API"
curl -u $user:$pass $target_uri/houses/refresh