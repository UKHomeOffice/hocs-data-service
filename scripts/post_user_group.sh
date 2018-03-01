#!/usr/bin/env bash

target_uri=${1}
data_dir=${2}
user=${3}
pass=${4}

alf_uri=${5}
user_group=${6}

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

echo "Posting Test Users"
curl -u $user:$pass -X POST $target_uri/users/dept/$user_group -F "file=@$data_dir/users/$user_group.csv" \
 -H "Content-Type: multipart/form-data"

if [ -n "$alf_uri" ]

then
    echo "Waiting for $alf_uri to come up"

    # Wait for the alfresco service to become available
    until curl -u $user:$pass -s $alf_uri/alfresco/faces/jsp/login.jsp > /dev/null
    do
        echo "Waiting for $alf_uri to come up"
        sleep 2
    done

    # Begin POSTs to the service to seed data
    echo "$alf_uri is up! publishing data"

    sleep 2

    echo "Publishing test users to Alfresco"
    curl -u $user:$pass $target_uri/users/dept/$user_group/publish
fi