#!/bin/bash

SELF_ABSOLUTE_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/$(basename "${BASH_SOURCE[0]}")"
WORKING_DIR=$(dirname $SELF_ABSOLUTE_PATH)

read_vmoptions() {
  vmoptions_file=`eval echo "$1" 2>/dev/null`
  if [ ! -r "$vmoptions_file" ]; then
    vmoptions_file="$prg_dir/$vmoptions_file"
  fi
  if [ -r "$vmoptions_file" ] && [ -f "$vmoptions_file" ]; then
    exec 8< "$vmoptions_file"
    while read cur_option<&8; do
      is_comment=`expr "W$cur_option" : 'W *#.*'`
      if [ "$is_comment" = "0" ]; then 
      	echo $cur_option
        vmo_include=`expr "W$cur_option" : 'W *-include-options \(.*\)'`
        if [ "$vmo_include" = "" ]; then
          JVM_OPTS="$JVM_OPTS $cur_option"
        fi
      fi
    done
    exec 8<&-
    if [ ! "$vmo_include" = "" ]; then
      read_vmoptions "$vmo_include"
    fi
  fi
}

JVM_OPTS=""
read_vmoptions $WORKING_DIR/mario.vmoptions

MAIN_CLASS=com.mario.Mario
LOCAL_CLASSPATH="./lib/*:./extensions/__lib__/*"

java $JVM_OPTS -cp "$LOCAL_CLASSPATH" -Dworking.dir=$WORKING_DIR $MAIN_CLASS $@ > /dev/null 2>&1 &

echo "Mario service is running with pid: "$$