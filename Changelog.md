# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.3.2] - 25/05/2024

### Added

- added support for tracing

## [1.3.1] - 14/02/2022
- SDC-3879 Fix Vulnerabilities

## [1.3.0] - 20/12/2021

- SDC-3821 Align container image naming
    - remove redundant container image name prefix

## [1.2.2] - 02/08/2021

- SDC-3647 Fix vulnerabilities
    - spring.version 2.4.3 -> 2.5.0
    - apache.commons.compress.version 1.20 -> 1.21

## [1.2.1] - 07/06/2021

- Add logging validation response in debug mode

## [1.2.0] - 21/04/2021

- Add a configuration of logging level by 'LOG_LEVEL' system environment variable 
- Add Swagger with OpenAPI description
- Supported Helm Versions: 
    - Helm v3: 3.5.2, 3.4.1, 3.3.4

## [1.1.0] - 12/04/2021

- Remove mapping to helm v2 when version desired is not provided
- Remove Helm v2 clients from container
- Supported Helm Versions: 
    - Helm v3: 3.5.2, 3.4.1, 3.3.4

## [1.0.0] - 24/03/2021

- First container release
- Supported Helm Versions: 
    - Helm v3: 3.5.2, 3.4.1, 3.3.4,
    - Helm v2: 2.17.0, 2.14.3

## [0.0.1] - 23/03/2021

- Init Helm client validator project, which is dedicated to be used during Helm packages validation in time of VSP creation



