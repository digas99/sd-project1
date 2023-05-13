#!/bin/bash

REPS=1
LOGGING="logging"
unset -v BINPATH

# handle args
while getopts "hr:!p:l:" opt; do
  case $opt in
    h)
      echo "Usage: run.sh [-h] [-r <repetitions>] [-p <path-binaries>] [-l <logging-file>]"
      exit 0
      ;;
    l)
      LOGGING=$OPTARG
      ;;
    p)
      BINPATH=$OPTARG
      ;;
    r)
      REPS=$OPTARG
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      exit 1
      ;;
    :)
      echo "Option -$OPTARG requires an argument." >&2
      exit 1
      ;;
  esac
done

if [ -z "$BINPATH" ]; then
  echo "Missing path to binaries. Use -p <path-binaries>."
  exit 1
fi

echo "Running with $REPS repetitions and binaries in $BINPATH"

CLASSPATH=$PWD/$BINPATH

# add libs
for file in lib/*; do
  CLASSPATH=$CLASSPATH:$PWD/$file:
done

SUCCESS_REPS=0

TIMEFORMAT="The script run for %3R seconds"
time {
  for i in $(seq 1 $REPS); do
    echo "Iteration $i of the program"
    java -cp $CLASSPATH main.Assault $LOGGING
    if [ $? -ne 0 ]; then
      echo "Error running the program"
      break
    fi
    SUCCESS_REPS=$((SUCCESS_REPS+1))
  done
}

echo "Run $SUCCESS_REPS repetitions of the program with success"
