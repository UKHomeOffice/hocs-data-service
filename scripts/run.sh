#!/usr/bin/env bash

# Start the java process
./scripts/start.sh &
$PROCESS_1 = $! &
# Start the data seeding process
./scripts/seed_data.sh ${1} ${2} &
$PROCESS_2 = $!

# Wait for processes to complete
wait $PROCESS_1 $PROCESS_2