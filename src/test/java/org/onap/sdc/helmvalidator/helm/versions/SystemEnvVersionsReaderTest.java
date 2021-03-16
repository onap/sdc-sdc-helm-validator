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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemEnvVersionsReaderTest {

    private static final String SINGLE_VERSION_VALUE = "3.2.1";
    private static final String MULTIPLE_VERSIONS_VALUE = "3.2.1,2.0.0,1.0.0";
    private static final int EXPECTED_SIZE_ONE = 1;
    private static final int EXPECTED_SIZE_THREE = 3;

    @Spy
    private SystemEnvVersionsReader versionsReader;

    @Test
    void canReadSingleValue() {
        when(versionsReader.getSupportedVersionsFromEnv()).thenReturn(SINGLE_VERSION_VALUE);

        List<String> versions = versionsReader.readVersions();

        assertThat(versions).hasSize(EXPECTED_SIZE_ONE);
    }

    @Test
    void canReadMultipleValues() {
        when(versionsReader.getSupportedVersionsFromEnv()).thenReturn(MULTIPLE_VERSIONS_VALUE);

        List<String> versions = versionsReader.readVersions();

        assertThat(versions).hasSize(EXPECTED_SIZE_THREE);
    }

    @Test
    void returnEmptyListWhenVariableIsNotPresent() {
        when(versionsReader.getSupportedVersionsFromEnv()).thenReturn("");

        List<String> versions = versionsReader.readVersions();

        assertThat(versions).isEmpty();
    }
}
