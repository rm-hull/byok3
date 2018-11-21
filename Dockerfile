FROM openjdk:11-slim
MAINTAINER Richard Hull <rm_hull@yahoo.co.uk>

ARG SCALA_VERSION=2.12
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app

RUN apt-get update && \
  apt-get install -y gnupg && \
  echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list && \
  apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 && \
  apt-get update && \
  apt-get install -y sbt && \
  sbt web/assembly && \
  mv web/target/scala-$SCALA_VERSION/byok3-web.jar byok3-web.jar && \
  rm -rf target project/target ~/.ivy2 /var/cache/apt/archives && \
  apt-get purge -y sbt gnupg && \
  apt-get autoremove -y --purge && \
  apt-get clean -y

EXPOSE 5000
ENTRYPOINT ["java", "-jar", "byok3-web.jar"]