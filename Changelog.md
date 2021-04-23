# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

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



