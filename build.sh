#!/bin/bash

### All constants goes here

### All functions should goes here

function print_usage()
{
   echo "Usage: ./build.sh"
} 

function build_image_stitcher()
{
   echo "Building project ..."
   mvn clean package

   if [ $? != "0" ]; then
      echo ""
      echo "Build failed."
      exit $?
   else
      echo ""
      echo "Build successful"
   fi
}

function build_tar()
{
   build_image_stitcher
   echo ""
   echo "Generating image-stitcher-1.0.tar.gz file ..."
   mvn dependency:build-classpath -Dmdep.outputFile=target/classpath.tmp > /dev/null
   cd target
   mkdir -p image-stitcher-1.0
   cp image-stitcher.sh image-stitcher-1.0
   chmod +x image-stitcher-1.0/image-stitcher.sh
   cp -r conf image-stitcher-1.0/conf
   cp classpath.tmp image-stitcher-1.0/conf/classpath.txt
   cp -r lib image-stitcher-1.0/lib
   cp image-stitcher-1.0.jar image-stitcher-1.0/image-stitcher-1.0.jar
   tar -zcvf image-stitcher-1.0.tar.gz image-stitcher-1.0 > /dev/null
   rm -r image-stitcher-1.0 > /dev/null
   rm classpath.tmp
   echo "Successful"
}

### Main starts here

if [ $# == 0 ]; then
   build_tar
   exit 0
else
   # no arguments found
   print_usage
fi

