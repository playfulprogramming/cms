# Stage 1: Cache Gradle dependencies
FROM gradle:8.11.1-jdk23-alpine AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
COPY *.gradle.kts gradle.properties /home/gradle/app/
COPY ./gradle /home/gradle/app/gradle
WORKDIR /home/gradle/app
RUN gradle clean build -i --stacktrace

# Stage 2: Build Application
FROM gradle:8.11.1-jdk23-alpine AS build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY . /usr/src/app/
WORKDIR /usr/src/app
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Build the fat JAR, Gradle also supports shadow
# and boot JAR by default.
RUN gradle buildFatJar --no-daemon

# Stage 3: Create the Runtime Image
FROM eclipse-temurin:23-jre-alpine AS runtime
EXPOSE 8080 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/server.jar
ENTRYPOINT ["java","-jar","/app/server.jar"]
