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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.sdc.helmvalidator.helm.versions.exception.NotSupportedApiVersionException;

@ExtendWith(MockitoExtension.class)
class ChartBasedVersionProviderTest {

    private final String testChartPath = "test/path";
    @Mock
    private SupportedVersionsProvider versionsProvider;
    @Mock
    private ApiVersionsReader apiVersionsReader;
    private ChartBasedVersionProvider chartBasedVersionProvider;

    @BeforeEach
    void setUp() {
        chartBasedVersionProvider = new ChartBasedVersionProvider(versionsProvider, apiVersionsReader);
    }

    @ParameterizedTest
    @CsvSource({"v1,2.18", "v2,3.11"})
    void shouldGetLatestHelmVersionBasedOnApiVersion(String apiVersion, String expectedHelmVersion) {
        when(apiVersionsReader.readVersion(testChartPath)).thenReturn(apiVersion);
        when(versionsProvider.getLatestVersion(Mockito.anyString())).thenReturn(expectedHelmVersion);

        String helmVersion = chartBasedVersionProvider.getVersion(testChartPath);

        assertThat(helmVersion).isEqualTo(expectedHelmVersion);
    }

    @Test
    void shouldThrowExceptionWhenApiVersionIsNotSupported() {
        when(apiVersionsReader.readVersion(testChartPath)).thenReturn("v3");

        Exception exception = assertThrows(NotSupportedApiVersionException.class,
            () -> chartBasedVersionProvider.getVersion(testChartPath));

        assertThat(exception).hasMessageContaining("Cannot obtain Helm version from API version: v3");
    }

}
