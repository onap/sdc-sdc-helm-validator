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
import org.springframework.stereotype.Service;

@Service
public class EnvProvider {

    public static EnvProvider getStandardProvider() {
        return new EnvProvider();
    }

    public String readEnvVariable(String envVariableName) {
        return Optional.ofNullable(getSystemEnv(envVariableName))
            .orElseGet(this::getDefaultValue);
    }

    private String getSystemEnv(String envVariableName) {
        return System.getenv(envVariableName);
    }

    private String getDefaultValue() {
        return LogLevel.getDefaultLevel();
    }

}
