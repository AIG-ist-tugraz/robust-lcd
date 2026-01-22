# Dockerfile - Build JAR files only (for developers)
# Usage:
#   docker build -t gc-seeker-build .
#   docker run --rm -v "$PWD/target":/output gc-seeker-build

FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Copy only files needed first (to leverage Docker layer caching for deps)
COPY pom.xml ./
COPY lib ./lib

# Pre-fetch dependencies (validate also installs local lib from ./lib)
RUN mvn -B -e -q validate dependency:go-offline --no-transfer-progress

# Now copy the rest of the source tree and build
COPY src ./src

# Build fat JARs (assembly plugin). Tests are skipped for faster image builds.
RUN mvn -B -e -DskipTests package --no-transfer-progress

# Copy JARs to /output when container runs
CMD ["sh", "-c", "cp /workspace/target/*-with-dependencies.jar /output/ && echo 'JAR files copied to /output:' && ls -la /output/*.jar"]
