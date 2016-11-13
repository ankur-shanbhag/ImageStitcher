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

## Installation on Linux environment (Local)
Following are the steps to setup and build the project on Ubuntu Linux environment using CLI. 
Please make sure that commands - `git, mvn, hadoop and java` can be executed directly from the command line. 
If not, please add them to the PATH environment variable.

1. Check out the project using - `git clone https://github.com/ankur-shanbhag/ImageStitcher.git`
2. Steps to build the project
  * `cp configuration.properties.template configuration.properties`
  * Open _configuration.properties_ and add values for all the properties as per instructions in the file.   
    Eg: __hadoop.home=/usr/local/hadoop-2.7.3/__  
    The properties related to GnuPlot are optional and only needed by developers for debugging (discussed below)
  * Build the project - `./image-stitcher.sh build`
3. Create an input directory with files containing configuration parameters required by clustering algorithm.
4. Run the project using - `./image-stitcher.sh [input directory path] [output directory path to be created]`

**NOTE:** If any of the steps does not work, verify all the required softwares are installed with specified versions. 

## Installation on AWS EMR
1. Start EMR cluster with Hadoop 2.7 or above.
2. SSH to AWS master node. For steps follow http://docs.aws.amazon.com/ElasticMapReduce/latest/DeveloperGuide/emr-connect-master-node-ssh.html
3. Install Git on master using `sudo yum install git-all`
4. Install Apache Maven using following commands
   * `sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo`
   * `sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo`
   * `sudo yum install -y apache-maven`
5. Copy input files, source and target image files EMR master using `scp`  
   Eg: `scp -i [keyFile].pem -r ~/input hadoop@[emr-master-dns]:/home/hadoop`
7. Create S3 bucket (recommended for working on EMR) or use HDFS as distributed storage. To use HDFS refer - http://stackoverflow.com/questions/22343918/how-do-i-use-hdfs-with-emr
6. Once you have copied all the required file and have `git` and `mvn` installed, the steps to checkout and build the project is same as local environment.  

**NOTE:** By default, Hadoop on EMR machines is installed at location `/usr/lib/hadoop`. Also, currently we do not support GnuPlot on EMR.

