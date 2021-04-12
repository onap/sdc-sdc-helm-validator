build:
	mvn clean install

build-docker:
	mvn clean install -Pdocker

build-docker-local:
	export HELM_SUPPORTED_VERSIONS=3.5.2,3.4.1,3.3.4 && \
	cd scripts && \
	./collect_helm_versions_from_web.sh local && \
	cd ../ && \
	mvn clean install -Pdocker-local

clean-local-files:
	rm -rf ./scripts/helm_tmp ./scripts/helm_versions

run-docker:
	docker run -p 8080:8080 onap/org.onap.sdc.sdc-helm-validator:latest
