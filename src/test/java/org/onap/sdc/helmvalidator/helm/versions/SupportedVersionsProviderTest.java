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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.sdc.helmvalidator.helm.validation.exception.NotSupportedVersionException;

@ExtendWith(MockitoExtension.class)
class SupportedVersionsProviderTest {

    private static final List<String> INSTALLED = List.of("3.9.0", "2.17.0", "3.14.4", "3.5.2");

    private SupportedVersionsProvider versionsProvider;

    @Mock
    private SystemEnvVersionsReader versionsReader;

    @Mock
    private InstalledHelmBinaries installedHelmBinaries;

    @BeforeEach
    void setUp() {
        lenient().when(installedHelmBinaries.versions()).thenReturn(new LinkedHashSet<>(INSTALLED));
        lenient().when(versionsReader.readVersions()).thenReturn(List.of());
        versionsProvider = new SupportedVersionsProvider(versionsReader, installedHelmBinaries);
    }

    @Test
    void allInstalledVersionsAreSupportedWhenNothingIsConfigured() {
        assertThat(versionsProvider.getVersions()).containsExactly("3.14.4", "3.9.0", "3.5.2", "2.17.0");
    }

    @Test
    void configuredVersionsFilterInstalledOnes() {
        when(versionsReader.readVersions()).thenReturn(List.of("3.5.2", "2.17.0"));

        assertThat(versionsProvider.getVersions()).containsExactly("3.5.2", "2.17.0");
    }

    @Test
    void configuredButNotInstalledIsNotAdvertised() {
        when(versionsReader.readVersions()).thenReturn(List.of("3.17.3", "3.5.2"));

        assertThat(versionsProvider.getVersions()).containsExactly("3.5.2");
    }

    @Test
    void emptyWhenNothingIsInstalled() {
        when(installedHelmBinaries.versions()).thenReturn(new LinkedHashSet<>());

        assertThat(versionsProvider.getVersions()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"2,2.17.0", "3,3.14.4"})
    void latestIsSemanticNotLexicographic(String major, String expected) {
        assertThat(versionsProvider.getLatestVersion(major)).isEqualTo(expected);
    }

    @Test
    void latestThrowsForMajorWithoutInstalledVersion() {
        assertThatExceptionOfType(NotSupportedVersionException.class)
            .isThrownBy(() -> versionsProvider.getLatestVersion("4"));
    }
}
