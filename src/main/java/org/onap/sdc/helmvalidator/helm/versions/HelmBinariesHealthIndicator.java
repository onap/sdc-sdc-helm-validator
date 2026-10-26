/*
 * ============LICENSE_START=======================================================
 * SDC-HELM-VALIDATOR
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import java.util.List;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class HelmBinariesHealthIndicator implements HealthIndicator {

    private final SupportedVersionsProvider supportedVersionsProvider;
    private final InstalledHelmBinaries installedHelmBinaries;

    public HelmBinariesHealthIndicator(SupportedVersionsProvider supportedVersionsProvider,
        InstalledHelmBinaries installedHelmBinaries) {
        this.supportedVersionsProvider = supportedVersionsProvider;
        this.installedHelmBinaries = installedHelmBinaries;
    }

    @Override
    public Health health() {
        List<String> versions = supportedVersionsProvider.getVersions();
        if (versions.isEmpty()) {
            return Health.down()
                .withDetail("reason", "no Helm binary on PATH matches HELM_SUPPORTED_VERSIONS")
                .withDetail("installed", installedHelmBinaries.versions())
                .build();
        }
        return Health.up().withDetail("versions", versions).build();
    }
}
