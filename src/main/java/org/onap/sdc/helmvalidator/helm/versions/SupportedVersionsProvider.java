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

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.onap.sdc.helmvalidator.helm.validation.exception.NotSupportedVersionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupportedVersionsProvider {

    private final SystemEnvVersionsReader versionsReader;

    @Autowired
    public SupportedVersionsProvider(SystemEnvVersionsReader versionsReader) {
        this.versionsReader = versionsReader;
    }

    /**
     * Retrieves list of available Helm client versions.
     *
     * @return list of available Helm client versions
     */
    public List<String> getVersions() {
        return versionsReader.readVersions().stream()
            .filter(Predicate.not(String::isBlank))
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());
    }

    /**
     * Retrieves latest available Helm client with given major version.
     *
     * @param helmMajorVersion major version of Helm client
     * @return latest available Helm client with given major version
     */
    public String getLatestVersion(String helmMajorVersion) {
        return getVersions().stream()
            .filter(supportedVersion -> supportedVersion.startsWith(helmMajorVersion + "."))
            .findFirst()
            .orElseThrow(() -> new NotSupportedVersionException(helmMajorVersion));
    }
}
