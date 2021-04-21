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

import java.util.HashMap;
import java.util.Map;

import java.util.Optional;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Service;


@Order()
@Service
public class LoggingEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String LOG_LEVEL_ENV = "LOG_LEVEL";
    private static final String DEFAULT_LOG_LEVEL = "ERROR";
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEnvironmentPostProcessor.class);


    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        String level = getLogLevel();
        Map<String, Object> loggingProperties = new HashMap<>();
        loggingProperties.put("logging.level.web", level);
        loggingProperties.put("logging.level.org.springframework", level);
        loggingProperties.put("logging.level.org.apache.catalina.core", level);
        loggingProperties.put("logging.level.org.onap.sdc.helmvalidator", level);
        loggingProperties.put("spring.mvc.log-request-details", isDebugLevel());

        environment.getPropertySources().addFirst(new MapPropertySource("logging-properties", loggingProperties));
    }


    private String getLogLevel() {
        return Optional.of(getLogLevelEnvVariable())
            .map(String::toUpperCase)
            .filter(this::isSupportedLevel)
            .orElseGet(LogLevel::getDefaultLevel);
    }

    private String getLogLevelEnvVariable() {
        return readEnvVariable(LOG_LEVEL_ENV);
    }

    private Boolean isDebugLevel() {
        return getLogLevel().equals("DEBUG");
    }

    private boolean isSupportedLevel(String level) {
        return LogLevel.getSupportedLevels().stream()
            .anyMatch(logLevel -> logLevel.equals(level));

    }

    @PostConstruct
    private void logConfigurationLevelValue() {
        String logLevel = getLogLevelEnvVariable();
        if (!isSupportedLevel(logLevel)) {
            LOGGER.error("Log level '{}' not match to supported levels. Available values:  {}", logLevel,
                LogLevel.getSupportedLevels());
            LOGGER.error("Set default level: {}", LogLevel.getDefaultLevel());
        }
    }


    public String readEnvVariable(String envVariableName) {
        return Optional.ofNullable(getSystemEnv(envVariableName))
            .orElseGet(this::getDefaultValue);
    }

    private String getSystemEnv(String envVariableName) {
        return System.getenv(envVariableName);
    }

    private String getDefaultValue() {
        return DEFAULT_LOG_LEVEL;
    }
}
