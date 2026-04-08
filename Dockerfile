FROM eclipse-temurin:11-alpine

ARG VERSION=${version}
ENV GROUP=onap
USER root

RUN addgroup $GROUP && adduser -G $GROUP -D validator \
    && apk update && apk upgrade --no-cache \
    && apk add --no-cache bash curl wget helm \
    && rm -rf /var/cache/apk/*

RUN mkdir /charts && chown -R validator:onap /charts

USER validator:onap

COPY target/sdc-helm-validator-${VERSION}.jar ./opt/helmvalidator/helmvalidator.jar

ENTRYPOINT ["java","-XX:+UseParallelGC","-XX:MinRAMPercentage=50","-XX:MaxRAMPercentage=50","-XX:MinHeapFreeRatio=10","-XX:MaxHeapFreeRatio=20","-jar","./opt/helmvalidator/helmvalidator.jar"]
