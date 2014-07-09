#!/bin/bash

############  ABOUT  ###########
# This script reads files in a backup directory and
# submits these readings to the EvenHub service, aka
# backup monitor service.
#
# Please use absolute path in command line argument -
# this will allow less confusion and more flexibility on
# the server side when validating backup files.
#
# Be careful when running on symlinked directories and
# NFS mounts - check that commands below work fine in
# such cases and get correct file status.
################################



############  STARTUP  ###########
echo "Running backup client script to submit status reading."

# Validate command line arguments
if [[ "$#" != 1 ]]; then
  echo "Wrong number of arguments supplied. Usage: submitBackupData.sh <absolute path to dir>"
  exit 1
fi



############  SETTINGS  ############
# Backup directory to use for reporting.
# Make sure you set this properly, preferrably providing absolute path.
BACKUP_DIR="$1"

# REST Url to submit data to. Comment it out to print data to stdout instead of sending to the service.
URL="http://localhost:8090/api/event"
API_USER="pgxbackup"
API_KEY="eR8rYKjT5bjWnWhTdvX6hrD+p271BpA3glk4lCkaCmQ="
# Users must be allowed to submit data to a project, Check project config in MongoDB for permissions.
PROJECT="backupmonitor"
HOST=$HOSTNAME


############  EXECUTION  ############
files=$(find $BACKUP_DIR -type f -exec stat --format '{ "filename": "%n", "sizeBytes": %s, "changedSecSinceEpoch": %Y }' {} \; | while read -r file; do echo "$file,"; done | sed '$s/.$//')


JSON="
{
  \"apiUser\": \"$API_USER\",
  \"apiKey\": \"$API_KEY\",
  \"project\": \"$PROJECT\",
  \"hostname\": \"$HOST\",
  \"files\": [ $files ]
}
"


############  SUBMIT RESULTS  #############
# Result output: STDOUT or HTTP
if [ -z "$URL" ]; then
  echo "$JSON"
else
  curl -X POST -H "Content-Type: application/json" -d "$JSON" "$URL"
fi

