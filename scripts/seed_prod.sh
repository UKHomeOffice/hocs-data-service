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

echo "Posting HMPO Topics"
curl -u $user:$pass -X POST $target_uri/topics/HMPO -F "file=@$data_dir/HMPO_Topics.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting Unit and Team structures"
curl -u $user:$pass -X POST $target_uri/units -F "file=@$data_dir/Unit_Team_Structure.csv" \
 -H "Content-Type: multipart/form-data"

echo "Posting Minister List"
curl -u $user:$pass -X POST $target_uri/list/ -d "@$data_dir/Minister_List.JSON" \
 -H "Content-Type: application/json"

echo "Pulling member information from external API"
curl -u $user:$pass $target_uri/houses/refresh

echo "Posting Users"
#curl -u $user:$pass -X POST $target_uri/users/dept/BICS -F "file=@$data_dir/users/BICS.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/CPFG -F "file=@$data_dir/users/CPFG.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/CR -F "file=@$data_dir/users/CR.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/DCU -F "file=@$data_dir/users/DCU.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/FOI -F "file=@$data_dir/users/FOI.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/HMPO_CCC -F "file=@$data_dir/users/HMPO_CCC.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/HMPO_Collectives -F "file=@$data_dir/users/HMPO_Collectives.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/HMPO_FOI -F "file=@$data_dir/users/HMPO_FOI.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/HO_Legal_Advisors -F "file=@$data_dir/users/HO_Legal_Adviser.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/Immigration_Enforcement -F "file=@$data_dir/users/Immigration_Enforcement.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/OSCT -F "file=@$data_dir/users/OSCT.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/Private_Office -F "file=@$data_dir/users/Private_Office.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/UKVI_ALS -F "file=@$data_dir/users/UKVI_ALS.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/UKVI_CCT -F "file=@$data_dir/users/UKVI_CCT.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/UKVI_RCM -F "file=@$data_dir/users/UKVI_RCM.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/UKVI_ROS -F "file=@$data_dir/users/UKVI_ROS.csv" \
# -H "Content-Type: multipart/form-data"
#curl -u $user:$pass -X POST $target_uri/users/dept/UKVI -F "file=@$data_dir/users/UKVI.csv" \
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

    echo "Publishing units to Alfresco"
    curl -u $user:$pass $target_uri/units/publish


    echo "Publishing users to Alfresco"
#    curl -u $user:$pass $target_uri/users/dept/BICS/publish
#    curl -u $user:$pass $target_uri/users/dept/CPFG/publish
#    curl -u $user:$pass $target_uri/users/dept/CR/publish
#    curl -u $user:$pass $target_uri/users/dept/DCU/publish
#    curl -u $user:$pass $target_uri/users/dept/FOI/publish
#    curl -u $user:$pass $target_uri/users/dept/HMPO_CCC/publish
#    curl -u $user:$pass $target_uri/users/dept/HMPO_Collectives/publish
#    curl -u $user:$pass $target_uri/users/dept/HMPO_FOI/publish
#    curl -u $user:$pass $target_uri/users/dept/HO_Legal_Advisors/publish
#    curl -u $user:$pass $target_uri/users/dept/Immigration_Enforcement/publish
#    curl -u $user:$pass $target_uri/users/dept/OSCT/publish
#    curl -u $user:$pass $target_uri/users/dept/Private_Office/publish
#    curl -u $user:$pass $target_uri/users/dept/UKVI_ALS/publish
#    curl -u $user:$pass $target_uri/users/dept/UKVI_CCT/publish
#    curl -u $user:$pass $target_uri/users/dept/UKVI_RCM/publish
#    curl -u $user:$pass $target_uri/users/dept/UKVI_ROS/publish
#    curl -u $user:$pass $target_uri/users/dept/UKVI/publish

fi