/*
 * ============LICENSE_START=======================================================
 * SDC-HELM-VALIDATOR
 * ================================================================================
 * Copyright (C) 2021 Nokia. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.sdc.helmvalidator.helm.versions;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SystemEnvVersionsReader {

    private static final String HELM_SUPPORTED_VERSIONS = "HELM_SUPPORTED_VERSIONS";
    private static final String DELIMITER = ",";

    List<String> readVersions() {
        return Arrays.stream(getSupportedVersionsFromEnv()
            .split(DELIMITER))
            .filter(Predicate.not(String::isBlank))
            .collect(Collectors.toList());
    }

    String getSupportedVersionsFromEnv() {
        return Optional.ofNullable(System.getenv(HELM_SUPPORTED_VERSIONS))
            .orElse("");
    }
}
