#!/usr/bin/env bash

target_uri=${1}
data_dir=${2}
user=${3}
pass=${4}

alf_uri=${5}

echo "Waiting for $target_uri to come up"
# Wait for the hocs data service to become available
until curl $user:$pass -s $target_uri/healthz > /dev/null
do
    echo "Waiting for $target_uri to come up"
    sleep 2
done

# Begin POSTs to the service to seed data
echo "$target_uri is up! Seeding data"

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

echo "Posting Test Users"
curl -u $user:$pass -X POST $target_uri/users/dept/TEST_USERS -F "file=@$data_dir/users/Test_Users.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting Minister List"
curl -u $user:$pass -X POST $target_uri/list/ -d "@$data_dir/Minister_List.JSON" \
 -H "Content-Type: application/json"

echo "Pulling member information from external API"
curl -u $user:$pass $target_uri/houses/refresh

if [ -n "$alf_uri" ]

then
    echo "Waiting for $alf_uri to come up"

    # Wait for the alfresco service to become available
    until curl -s $alf_uri/alfresco/faces/jsp/login.jsp > /dev/null
    do
        echo "Waiting for $alf_uri to come up"
        sleep 2
    done

    # Begin POSTs to the service to seed data
    echo "$alf_uri is up! publishing data"

    sleep 2

    echo "Publishing units to Alfresco"
    curl -u $user:$pass $target_uri/units/publish

    echo "Publishing test users to Alfresco"
    curl -u $user:$pass $target_uri/users/dept/TEST_USERS/publish
fi