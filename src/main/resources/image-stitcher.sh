#!/bin/bash

### All constants goes here

### All functions should goes here

function print_usage()
{
   echo "Usage: image-stitcher.sh [run | plot]"
} 

function plot_data()
{
   outputFilePath=$1
   plot3D=$2
   superimpose=$3
   
   if [[ -z "$outputFilePath" || -z "$plot3D" || -z "$superimpose" ]]; then
      echo "Usage: image-stitcher.sh plot [output-file] [plot3D] [superimpose]"
      echo "Where  output-file is generated part file"
      echo "       plot3D is [true | false]"
      echo "       superimpose is [true | false]"
      echo ""
      echo "Example: ./image-stitcher.sh plot /home/hadoop/output/part-r-00000 true true"
      exit 0     
   fi

   java -cp image-stitcher-1.0.jar:`cat conf/classpath.txt` neu.nctracer.data.plot.Client $outputFilePath $plot3D $superimpose

   if [ $? != "0" ]; then
      echo "Error while plotting scatter plot"
      exit 0
   fi
}

function run_image_stitch
{
   arguments=$1
   hadoop jar image-stitcher-1.0.jar $arguments
   
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

   plot)
     plot_data $2 $3 $4
     ;;
    
   run)
     arguments=`echo ${@:2}`
     run_image_stitch "$arguments"
     ;;

   *)
     echo "Invalid command found"
     echo ""
     print_usage
     exit 0
esac
