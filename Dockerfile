FROM sbtscala/scala-sbt:eclipse-temurin-11.0.16_1.7.3_2.13.10 AS safe-separator-decomposition

# The GitHub token that grants access to the private repository that stores the dependencies of the Scala project.
ARG GITHUB_TOKEN

WORKDIR /usr/local/src/safe-separator-decomposition

COPY build.sbt .
COPY ./src ./src
COPY ./project/assembly.sbt ./project/assembly.sbt
COPY ./project/plugins.sbt ./project/plugins.sbt
COPY ./project/build.properties ./project/build.properties

RUN sbt assembly

# -------------------------------------------

FROM eclipse-temurin:11.0.16.1_1-jdk AS jre-build

# Create a custom Java runtime environment.
RUN $JAVA_HOME/bin/jlink \
         --add-modules java.base,java.desktop,java.logging,java.management,java.naming,java.sql,java.xml,jdk.jfr,jdk.unsupported \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime

# -------------------------------------------

FROM mono:6.12 AS bztreewidth

# Update repository configuration, since the Mono image relies on an old Debian distribution.
RUN echo "deb http://archive.debian.org/debian buster main" > /etc/apt/sources.list

# Install Git for cloning the GitHub repository that contains the source files.
# Make is used to compile the source code.
# G++ is used by the Makefile to compile a test script.
RUN apt-get -y update && \
    apt-get install -y make g++

# Change the working directory to where the source files will be downloaded.
WORKDIR /usr/local/src

ADD https://github.com/TomvdZanden/BZTreewidth/archive/bfbf736a98eea412c7ba2de4a21280816d9b8bbb.tar.gz ./

RUN tar -xf bfbf736a98eea412c7ba2de4a21280816d9b8bbb.tar.gz

WORKDIR /usr/local/src/BZTreewidth-bfbf736a98eea412c7ba2de4a21280816d9b8bbb

# Run the Makefile, which compiles the source files into multiple executables.
#RUN mcs -optimize -define:SEQUENTIAL -out:bin/BZTreewidth-DP BZTreewidth/*.cs
RUN make

# -------------------------------------------

# The final image uses the slim tag as it only requires the runtime environment.
FROM mono:6.12-slim

# Set Java Home directory.
ENV JAVA_HOME=/opt/java/openjdk

# Add Java Home to PATH.
ENV PATH "${JAVA_HOME}/bin:${PATH}"

# Copy Java runtime environment.
COPY --from=jre-build /javaruntime $JAVA_HOME

# Set the working directory.
WORKDIR /usr/local/bin

# Copy the executables and the graph instances to the final image.
# TODO: Instead of copying the JAR file, copy the source files and compile inside the container.
COPY --from=safe-separator-decomposition /usr/local/src/safe-separator-decomposition/target/scala-3.7.2/safe-separator-decomposition-assembly-0.1.0-SNAPSHOT.jar .

# Copy the shell script that allow for a convenient way to run BZTreewidth.
COPY ./bin/bztreewidth.sh ./

# Make the shell script executable.
RUN chmod +x bztreewidth.sh

# Copy the executable of BZTreewidth.
COPY --from=bztreewidth /usr/local/src/BZTreewidth-bfbf736a98eea412c7ba2de4a21280816d9b8bbb/bin .

ENTRYPOINT ["java", "-Xmx5500m", "-cp", "/usr/local/bin/safe-separator-decomposition-assembly-0.1.0-SNAPSHOT.jar", "nl.rug.ds.experiments.safe.separators.main.Main"]