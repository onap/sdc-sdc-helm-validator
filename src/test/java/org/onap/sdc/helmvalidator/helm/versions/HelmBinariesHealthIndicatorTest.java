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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

@ExtendWith(MockitoExtension.class)
class HelmBinariesHealthIndicatorTest {

    @Mock
    private SupportedVersionsProvider supportedVersionsProvider;

    @Mock
    private InstalledHelmBinaries installedHelmBinaries;

    @InjectMocks
    private HelmBinariesHealthIndicator indicator;

    @Test
    void upWithSupportedVersions() {
        when(supportedVersionsProvider.getVersions()).thenReturn(List.of("3.19.0"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("versions", List.of("3.19.0"));
    }

    @Test
    void downWhenNoVersionIsSupported() {
        when(supportedVersionsProvider.getVersions()).thenReturn(List.of());
        Set<String> installed = new LinkedHashSet<>(List.of("3.19.0"));
        when(installedHelmBinaries.versions()).thenReturn(installed);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("installed", installed);
    }
}
