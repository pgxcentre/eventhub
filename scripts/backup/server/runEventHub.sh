#!/bin/bash

echo
echo "Starting the EVENT HUB service." 
echo

INSTALL_DIR="/opt/pgxtools/eventhub"

# Set $customSettingsFile to a file path of a props file if you want to
# override default application settings.
#
# By default props files are packaged in the jar, but can be overriden
# by JVM command line arguments as done here for convenience.
#
# The props file format follows this form:
# key=value
#
# To get a list of available application properties/settings consult source
# code or unzip the jar file and look at the *.props files.
customSettingsFile="$INSTALL_DIR/conf/custom.props"
if [ -z $customSettingsFile ]; then
  settings=""
else
  settings="-Dpropsfile=$customSettingsFile"
fi


# Set any JVM compatible arguments here:
JVM_ARGS="-server "

##### ??????????? ALL CODE BELOW NEEDS TO BE FIXED / CHECKED ??????????? ##########

# Run in Production environment:
#
# This is obsolete way to run application.
# java $JVM_ARGS $settings -Drun.mode=production -jar rest-assembly-0.1.jar "$@"  >> pregene_service.log &
#
# You have to specify directory path to the 'mongeez.xml' file location. This file
# can't be empty and must exist. It should at least contain:
#
#     <changeFiles></changeFiles>
#
# For more information on Mongeez file format see:
# http://secondmarket.github.io/mongeez/
# https://github.com/secondmarket/mongeez
# or check SBT dependency location if these links break in the future.
mongeezPath="$INSTALL_DIR/conf/mongeez"
CLASSPATH="$mongeezPath:$INSTALL_DIR/bin/rest-assembly-0.1.jar"
java -classpath "$CLASSPATH" $JVM_ARGS $settings -Drun.mode=production ca.pgx.rest.boot.Boot "$@" &

