# helm-client-validator

Docker container with Spring-Boot application for validating helm charts.

#### Development
To build image use:
```
make build-docker
```
or
```
make build-docker-local
```
##### Modification helm versions
To modify helm versions: 

1. Modify `Dockerfile`, add desired version in line:
```shell script
ENV HELM_SUPPORTED_VERSIONS=3.5.2,3.4.1,3.3.4,2.17.0,2.14.3
```
2. In local development, edit file: `Makefile` and add desired version in lines:
```shell script
build-docker-local:
	export HELM_SUPPORTED_VERSIONS=3.5.2,3.4.1,3.3.4,2.17.0
```
If you want to clean downloaded files run: `make clean-local-files`

##### Run container locally 
In order to run docker container locally use: 
```
make run-docker
```
Example charts are located in the following directory:
```
./dev-resources/sample-charts
```
##### Change log level
To change log level by system environment add to dockerfile following code:
```
ENV LOG_LEVEL=<expected level e.g. DEBUG>
```
or run container with LOG_LEVEL ENV 
```
docker run -p 8080:8080 -e LOG_LEVEL=INFO onap/org.onap.sdc.sdc-helm-validator:latest
```

## Available endpoints
* Chart validation:

    `http://localhost:[PORT]/validate`

#### Request
This should be a POST with `multipart/form-data` encoding with the following fields:

        "versionDesired": [String] - helm version, which will be used for validation. If param not provided then version will be taken from apiVersion
        "file": [FILE] - helm chart to be validated packed in .txz format
        "isLinted": ["true"/"false"] - if false, there will be an attempt to render the chart without linting it first        
        "isStrictLinted": ["true"/"false"] - linting should be strict or not

It is possible to provide helm version in three ways:
1. Select exact version from supported versions e.g. "3.4.1"
2. Select major helm version e.g. "v3" for Helm 3. In this case app will use the latest supported version.
3. In case when field version is not present in request then app gets helm version from "apiVersion" field located in the main chart (Chart.yaml).
Mapping rules:

apiVersion: v1 -> the latest available helm 3
apiVersion: v2 -> the latest available helm 3

#### Response
Json with the following fields:
        
        "valid": ["true"/"false"] - passed linting without errors
        "deployable": ["true"/"false"] - passed template rendering without errors
        "lintErrors": [ARRAY OF STRINGS] - linting errors
        "lintWarnings": [ARRAY OF STRINGS] - linting warnings
        "renderErrors": [ARRAY OF STRINGS] - rendering errors

* Supported versions [GET]

    `http://localhost:[PORT]/versions` 

#### Response
Following Json:

        [ARRAY OF STRINGS] - supported helm versions
