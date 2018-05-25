# Ubuntu 16.04
# Oracle Java 10.0.1 64 bit
# Maven 3.5.3

FROM ubuntu:16.04

MAINTAINER Kai Winter (https://github.com/kaiwinter)

# this is a non-interactive automated build - avoid some warning messages
ENV DEBIAN_FRONTEND noninteractive

# update dpkg repositories
RUN apt-get update 

# install wget
RUN apt-get install -y wget

# get maven 3.5.3
RUN wget --no-verbose -O /tmp/apache-maven-3.5.3.tar.gz http://archive.apache.org/dist/maven/maven-3/3.5.3/binaries/apache-maven-3.5.3-bin.tar.gz

# install maven
RUN tar xzf /tmp/apache-maven-3.5.3.tar.gz -C /opt/
RUN ln -s /opt/apache-maven-3.5.3 /opt/maven
RUN ln -s /opt/maven/bin/mvn /usr/local/bin
RUN rm -f /tmp/apache-maven-3.5.3.tar.gz
ENV MAVEN_HOME /opt/maven

# remove download archive files
RUN apt-get clean

# set shell variables for java installation
ENV java_version 10.0.1
ENV filename jdk-10.0.1_linux-x64_bin.tar.gz
ENV downloadlink http://download.oracle.com/otn-pub/java/jdk/10.0.1+10/fb4372174a714e6b8c52526dc134031e/$filename

# download java, accepting the license agreement
RUN wget -c --header "Cookie: oraclelicense=accept-securebackup-cookie" -O /tmp/$filename $downloadlink 

# unpack java
RUN mkdir /opt/java-oracle && tar -zxf /tmp/$filename -C /opt/java-oracle/
ENV JAVA_HOME /opt/java-oracle/jdk-$java_version
ENV PATH $JAVA_HOME/bin:$PATH

# configure symbolic links for the java and javac executables
RUN update-alternatives --install /usr/bin/java java $JAVA_HOME/bin/java 20000 && update-alternatives --install /usr/bin/javac javac $JAVA_HOME/bin/javac 20000

CMD [""]
