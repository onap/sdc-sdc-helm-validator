# Builder stage: install Go + Helm
FROM golang:alpine AS builder
RUN apk add --no-cache bash curl wget

ENV HELM_SUPPORTED_VERSIONS=3.14.4
COPY scripts/collect_helm_versions_from_web.sh /tmp/collect_helm_versions_from_web.sh
RUN chmod +x /tmp/collect_helm_versions_from_web.sh && /tmp/collect_helm_versions_from_web.sh

# Runtime stage: only Java + JAR
FROM eclipse-temurin:11-alpine
ARG VERSION=${version}
ENV GROUP=onap
USER root

RUN addgroup $GROUP && adduser -G $GROUP -D validator \
    && apk add --no-cache bash curl wget \
    && apk upgrade --no-cache

RUN mkdir /charts && chown -R validator:onap /charts
USER validator:onap

COPY target/sdc-helm-validator-${VERSION}.jar ./opt/helmvalidator/helmvalidator.jar

ENTRYPOINT ["java","-XX:+UseParallelGC","-XX:MinRAMPercentage=50","-XX:MaxRAMPercentage=50","-XX:MinHeapFreeRatio=10","-XX:MaxHeapFreeRatio=20","-jar","./opt/helmvalidator/helmvalidator.jar"]
