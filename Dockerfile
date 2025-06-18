FROM eclipse-temurin:11-alpine

ARG VERSION=${version}
ENV GROUP=onap
USER root
RUN addgroup $GROUP && adduser -G $GROUP -D validator

RUN apk add --no-cache bash vim curl wget

ENV HELM_SUPPORTED_VERSIONS=3.5.2,3.4.1,3.3.4

#Installing Helm
COPY scripts/collect_helm_versions_from_web.sh ./opt/helmvalidator/tmp/collect_helm_versions_from_web.sh
RUN chmod +x ./opt/helmvalidator/tmp/collect_helm_versions_from_web.sh
RUN ./opt/helmvalidator/tmp/collect_helm_versions_from_web.sh
RUN rm -r ./opt/helmvalidator/tmp

RUN mkdir /charts
RUN chown -R validator:onap /charts

USER validator:onap

COPY target/sdc-helm-validator-${VERSION}.jar ./opt/helmvalidator/helmvalidator.jar

ENTRYPOINT ["java","-XX:+UseParallelGC","-XX:MinRAMPercentage=50","-XX:MaxRAMPercentage=50","-XX:MinHeapFreeRatio=10","-XX:MaxHeapFreeRatio=20","-jar","./opt/helmvalidator/helmvalidator.jar"]
