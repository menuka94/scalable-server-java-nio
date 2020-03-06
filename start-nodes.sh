#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"
JAR_NAME="cs455-hw2-1.0-SNAPSHOT"
JAR_PATH="$DIR/conf/:$DIR/build/libs/$JAR_NAME.jar"
MACHINE_LIST="$DIR/conf/machine_list"
SCRIPT="java -cp $JAR_PATH cs455.scaling.client.Client ${1} 5600 50"
COMMAND='gnome-terminal --geometry=200x40'
for machine in $(cat ${MACHINE_LIST})
do
OPTION='--tab -t "'$machine'" -e "ssh -t '$machine' cd '$DIR'; echo '$SCRIPT'; '$SCRIPT'"'
COMMAND+=" $OPTION"
done
eval $COMMAND &