#!/bin/bash

### All constants goes here

### All functions should goes here

function print_usage()
{
   echo "Usage: image-stitcher.sh [build | run | plot]"
} 


function build_image_stitcher()
{
   echo "Building project ..."
   mvn clean package

   if [ $? != "0" ]; then
      echo "Build failed."
      exit $?
   else
      echo "Build successful"
   fi
}

function plot_data()
{
   outputFilePath=$1
   mvn dependency:build-classpath -Dmdep.outputFile=classpath.tmp > /dev/null
   java -cp target/image-stitcher-1.0.jar:`cat classpath.tmp` neu.nctracer.data.plot.Client $outputFilePath true false

   if [ $? != "0" ]; then
      echo "Error while plotting scatter plot"
      rm classpath.tmp
      exit 0
   else
      rm classpath.tmp
   fi
}

function run_image_stitch
{
   hadoop jar target/image-stitcher-1.0.jar /home/ankur/input/ output
   
   if [ $? != "0" ]; then
      echo "Execution failed. Something went wrong. Please check logs for details."
      exit 0
   fi
}

### Main starts here

if [ $# == 0 ]; then
   # no arguments found
   print_usage
   exit 0
fi

case $1 in
   build)
     build_image_stitcher
     ;;

   plot)
     plot_data $2
     ;;
    
   run)
     run_image_stitch
     ;;

   *)
     echo "Invalid command found ..."
     print_usage
     exit 0
esac

