FROM frolvlad/alpine-oraclejdk8:latest
MAINTAINER Richard Hull <rm_hull@yahoo.co.uk>

ARG APP_VERSION=0.1.0
ARG SCALA_VERSION=2.12
ARG SBT_DOWNLOAD_URL=https://cocl.us/sbt01316tgz

RUN apk add --no-cache bash

RUN apk add --no-cache --virtual=build-dependencies curl && \
    curl -sL $SBT_DOWNLOAD_URL | gunzip | tar -x -C /usr/local && \
    ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt && \
    chmod 0755 /usr/local/bin/sbt && \
    apk del build-dependencies && \
    rm -rf /tmp/* /var/cache/apk/*

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app
RUN \
  sbt assembly && \
  mv target/scala-$SCALA_VERSION/byok3-assembly-$APP_VERSION.jar . && \
  rm -rf target project/target ~/.ivy2

EXPOSE 3000
ENTRYPOINT ["java", "-jar", "byok3-assembly-$APP_VERSION.jar"]