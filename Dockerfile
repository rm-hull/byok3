FROM eclipse-temurin:21-jammy
MAINTAINER Richard Hull <rm_hull@yahoo.co.uk>

ARG SCALA_VERSION=2.12
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app

RUN apt-get update && \
  apt-get install -y apt-transport-https curl gnupg && \
  echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
  echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list && \
  curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | gpg --no-default-keyring --keyring gnupg-ring:/etc/apt/trusted.gpg.d/scalasbt-release.gpg --import && \
  chmod 644 /etc/apt/trusted.gpg.d/scalasbt-release.gpg && \
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