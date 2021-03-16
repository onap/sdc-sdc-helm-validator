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
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupportedVersionsProviderTest {

    private static final List<String> UNSORTED_VERSIONS = List.of("1.0.0", "2.2.1", "2.0.0", "3.0.0");
    private static final List<String> SORTED_VERSIONS = List.of("3.0.0", "2.2.1", "2.0.0", "1.0.0");
    private static final List<String> SUPPORTED_VERSIONS = List.of("4.2.0", "3.11.3", "3.1.0", "3.0.4", "2.18.2",
        "2.1.5", "1.5.6");

    private SupportedVersionsProvider versionsProvider;

    @Mock
    private SystemEnvVersionsReader versionsReader;

    @BeforeEach
    void setUp() {
        versionsProvider = new SupportedVersionsProvider(versionsReader);
    }

    @Test
    void shouldReturnSortedVersionsInRevertOrder() {

        when(versionsReader.readVersions()).thenReturn(UNSORTED_VERSIONS);

        List<String> versions = versionsProvider.getVersions();

        assertThat(versions).isEqualTo(SORTED_VERSIONS);
    }

    @ParameterizedTest
    @CsvSource({"2,2.18.2", "3,3.11.3"})
    void shouldGetLatestHelmVersionBasedOnDesiredMajorVersion(String desiredMajorVersion, String expectedHelmVersion) {
        when(versionsProvider.getVersions()).thenReturn(SUPPORTED_VERSIONS);

        String helmVersion = versionsProvider.getLatestVersion(desiredMajorVersion);

        assertThat(helmVersion).isEqualTo(expectedHelmVersion);
    }
}
