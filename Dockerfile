FROM sbtscala/scala-sbt:eclipse-temurin-24.0.1_9_1.11.5_3.7.2 AS safe-separator-decomposition

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

FROM eclipse-temurin:24.0.1_9-jdk AS jre-build

# Create a custom Java runtime environment.
RUN $JAVA_HOME/bin/jlink \
         --add-modules java.base,java.desktop,java.logging,java.management,java.naming,java.sql,java.xml,jdk.jfr,jdk.unsupported \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime

# -------------------------------------------

FROM debian:trixie-slim AS bztreewidth

# Install Mono for compilation of the source code.
RUN apt-get update && \
    apt-get install -y mono-complete && \
    rm -rf /var/lib/apt/lists/*

# Change the working directory to where the source files will be downloaded.
WORKDIR /usr/local/src

# Download the source code of the BZTreewidth algorithm from GitHub.
ADD https://github.com/TomvdZanden/BZTreewidth/archive/bfbf736a98eea412c7ba2de4a21280816d9b8bbb.tar.gz ./

RUN tar -xf bfbf736a98eea412c7ba2de4a21280816d9b8bbb.tar.gz

WORKDIR /usr/local/src/BZTreewidth-bfbf736a98eea412c7ba2de4a21280816d9b8bbb

# Create output folder and compile all binaries as done in the Makefile. The Makefile cannot be used directly,
# because compiling td-validate.cpp fails: it relies on std::numeric_limits, which is not explicitly
# included.
RUN mkdir -p bin
RUN dmcs -optimize -define:SEQUENTIAL -out:bin/BZTreewidth-DP.exe BZTreewidth/*.cs && \
    dmcs -optimize -define:SEQUENTIAL\;LIMIT_SEPARATOR -out:bin/BZTreewidth-DP-LS.exe BZTreewidth/*.cs

# -------------------------------------------

# The final image uses the slim tag as it only requires the Mono runtime environment.
FROM mono:6.12-slim

# Set Java Home directory.
ENV JAVA_HOME=/opt/java/openjdk

# Add Java Home to PATH.
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Copy Java runtime environment.
COPY --from=jre-build /javaruntime $JAVA_HOME

# Set the working directory.
WORKDIR /usr/local/bin

# Copy the executables and the graph instances to the final image.
# TODO: Instead of copying the JAR file, copy the source files and compile inside the container.
COPY --from=safe-separator-decomposition /usr/local/src/safe-separator-decomposition/target/scala-3.7.2/safe-separator-decomposition-assembly-0.1.0-SNAPSHOT.jar .

# Copy the shell script that allows for a convenient way to run BZTreewidth.
COPY ./bin/bztreewidth.sh ./

# Make the shell script executable.
RUN chmod +x bztreewidth.sh

# Copy the executables of BZTreewidth.
COPY --from=bztreewidth /usr/local/src/BZTreewidth-bfbf736a98eea412c7ba2de4a21280816d9b8bbb/bin .

ENTRYPOINT ["java", "-Xmx5500m", "-cp", "/usr/local/bin/safe-separator-decomposition-assembly-0.1.0-SNAPSHOT.jar", "nl.rug.ds.experiments.safe.separators.main.Main"]