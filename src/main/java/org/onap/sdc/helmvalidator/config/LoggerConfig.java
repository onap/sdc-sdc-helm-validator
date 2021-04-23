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

import java.util.Optional;
import java.util.Properties;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggerConfig {

    private static final String LOG_LEVEL_ENV = "LOG_LEVEL";

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerConfig.class);

    private final EnvProvider envProvider;

    public LoggerConfig(EnvProvider envProvider) {
        this.envProvider = envProvider;
    }

    /**
     * Get logger properties
     *
     * @return Logger properties with values from envs
     */
    public Properties getLoggerProperties() {
        var level = getLogLevel();
        var loggerProperties = new Properties();
        loggerProperties.setProperty("logging.level.web", level);
        loggerProperties.setProperty("logging.level.org.springframework", level);
        loggerProperties.setProperty("logging.level.org.apache.catalina.core", level);
        loggerProperties.setProperty("logging.level.org.onap.sdc.helmvalidator", level);
        loggerProperties.setProperty("spring.mvc.log-request-details", isDebugLevel().toString());

        return loggerProperties;
    }

    private String getLogLevel() {
        return Optional.of(getLogLevelEnvVariable())
            .map(String::toUpperCase)
            .filter(this::isSupportedLevel)
            .orElseGet(LogLevel::getDefaultLevel);
    }

    private String getLogLevelEnvVariable() {
        return envProvider.readEnvVariable(LOG_LEVEL_ENV);
    }

    private Boolean isDebugLevel() {
        return getLogLevel().equals("DEBUG");
    }

    private boolean isSupportedLevel(String level) {
        return LogLevel.getSupportedLevels().stream()
            .anyMatch(logLevel -> logLevel.equalsIgnoreCase(level));
    }

    @PostConstruct
    private void logConfigurationLevelValue() {
        String logLevel = getLogLevelEnvVariable();
        if (!isSupportedLevel(logLevel)) {
            LOGGER.error("Log level '{}' not match to supported levels. Available values:  {}", logLevel,
                LogLevel.getSupportedLevels());
            LOGGER.error("Log level set to default: {}", LogLevel.getDefaultLevel());
        }
    }
}
