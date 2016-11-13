# ImageStitcher

The goal of the project is to explore how to "stitch together" 2 stacks
of images capturing nerve cell structure in different regions of a
brain. 

## Minimum requirements:
1. Linux is supported as a development and production platform
2. Java 7 or above. You can download latest Oracle JDK from -
http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html
3. Apache Hadoop 2.7.3
4. Apache Maven 3.3.9 or above
5. Git client

## Installation
Following are the steps to setup and build the project on Ubuntu Linux environment using CLI. 
Please make sure that commands - `git, mvn, hadoop and java` can be executed directly from the command line. 
If not, please add them to the PATH environment variable.

1. Check out the project using - `git clone https://github.com/ankur-shanbhag/ImageStitcher.git`
2. Steps to build the project
  * `cp configuration.properties.template configuration.properties`
  * Open _configuration.properties_ and add values for all the properties as per instructions in the file.   
    Eg: __hadoop.home=/usr/local/hadoop-2.7.3/__  
    The properties related to GNU plot are optional and only needed by developers for debugging (discussed below)
  * Build the project - `./image-stitcher.sh build`
3. Create an input directory with files containing configuration parameters required by clustering algorithm.
4. Run the project using - `./image-stitcher.sh [input directory path] [output directory path to be created]`

**NOTE:** If any of the steps does not work, verify all the required softwares are installed with specified versions. 
