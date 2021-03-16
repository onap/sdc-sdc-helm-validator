FROM nexus3.onap.org:10001/onap/integration-java11:8.0.0

ARG VERSION=${version}

USER root
RUN adduser -G onap -D validator

RUN apk add --no-cache bash vim curl wget

ENV HELM_SUPPORTED_VERSIONS=3.5.2,3.4.1,3.3.4,2.17.0,2.14.3

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
