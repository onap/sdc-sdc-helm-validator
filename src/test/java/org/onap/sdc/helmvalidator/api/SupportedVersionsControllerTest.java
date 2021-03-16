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

package org.onap.sdc.helmvalidator.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.sdc.helmvalidator.helm.versions.SupportedVersionsProvider;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SupportedVersionsControllerTest {

    private static final String SAMPLE_VERSION1 = "3.4.1";
    private static final String SAMPLE_VERSION2 = "3.3.4";
    private static final String SAMPLE_VERSION3 = "2.17.0";
    private static final String VERSIONS = "versions";
    private static final int EXPECTED_SIZE = 3;

    private SupportedVersionsController supportedVersionsController;

    @Mock
    private SupportedVersionsProvider versionsProvider;

    @BeforeEach
    void setUp() {
        supportedVersionsController = new SupportedVersionsController(versionsProvider);
    }

    @Test
    void shouldReturnVersions() {
        when(versionsProvider.getVersions()).thenReturn(List.of(SAMPLE_VERSION1, SAMPLE_VERSION2, SAMPLE_VERSION3));

        ResponseEntity<Map<String, List<String>>> supportedVersionsResponse = supportedVersionsController
            .supportedVersions();
        List<String> supportedVersions = supportedVersionsResponse.getBody().get(VERSIONS);

        assertThat(supportedVersions).isNotNull();
        assertThat(supportedVersions).hasSize(EXPECTED_SIZE);
        assertThat(supportedVersions).contains(SAMPLE_VERSION1, SAMPLE_VERSION2, SAMPLE_VERSION3);
    }
}
