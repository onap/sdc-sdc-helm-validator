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

package org.onap.sdc.helmvalidator.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;
import org.junit.jupiter.api.Test;

class LoggerConfigTest {

    private static final boolean WITHOUT_DETAILS = false;
    private static final boolean WITH_DETAILS = true;
    private static final String EXPECTED_ERROR = "ERROR";
    private static final String EXPECTED_DEBUG = "DEBUG";

    private final EnvProvider envProvider = mock(EnvProvider.class);
    private final LoggerConfig loggerConfig = new LoggerConfig(envProvider);

    @Test
    void shouldProvideCorrectPropertiesForErrorLogLevel() {
        when(envProvider.readEnvVariable(anyString())).thenReturn("ERROR");

        Properties properties = loggerConfig.getLoggerProperties();

        assertThatAllLogPropertiesHasExpectedValue(properties, EXPECTED_ERROR);
        assertThatRequestDetailsPropertyHasExpectedValue(properties, WITHOUT_DETAILS);
    }

    @Test
    void shouldProvideCorrectPropertiesForEnvInLowerCase() {
        when(envProvider.readEnvVariable(anyString())).thenReturn("error");

        Properties properties = loggerConfig.getLoggerProperties();

        assertThatAllLogPropertiesHasExpectedValue(properties, EXPECTED_ERROR);
        assertThatRequestDetailsPropertyHasExpectedValue(properties, WITHOUT_DETAILS);
    }

    @Test
    void shouldProvideCorrectPropertiesForDebugLevel() {
        when(envProvider.readEnvVariable(anyString())).thenReturn("DEBUG");

        Properties properties = loggerConfig.getLoggerProperties();

        assertThatAllLogPropertiesHasExpectedValue(properties, EXPECTED_DEBUG);
        assertThatRequestDetailsPropertyHasExpectedValue(properties, WITH_DETAILS);
    }

    @Test
    void shouldProvideCorrectPropertiesForNotSupportedLevel() {
        when(envProvider.readEnvVariable(anyString())).thenReturn("notSupportedLevel");

        Properties properties = loggerConfig.getLoggerProperties();

        assertThatAllLogPropertiesHasExpectedValue(properties, "ERROR");
        assertThatRequestDetailsPropertyHasExpectedValue(properties, WITHOUT_DETAILS);
    }

    private void assertThatAllLogPropertiesHasExpectedValue(Properties properties, String expectedValue) {
        assertThat(properties.getProperty("logging.level.web")).isEqualTo(expectedValue);
        assertThat(properties.getProperty("logging.level.org.springframework")).isEqualTo(expectedValue);
        assertThat(properties.getProperty("logging.level.org.springdoc")).isEqualTo(expectedValue);
        assertThat(properties.getProperty("logging.level.org.apache.catalina.core")).isEqualTo(expectedValue);
        assertThat(properties.getProperty("logging.level.org.apache.tomcat")).isEqualTo(expectedValue);
        assertThat(properties.getProperty("logging.level.org.onap.sdc.helmvalidator")).isEqualTo(expectedValue);
    }

    private void assertThatRequestDetailsPropertyHasExpectedValue(Properties properties, Boolean expected) {
        assertThat(properties.getProperty("spring.mvc.log-request-details")).isEqualTo(expected.toString());
    }

}
