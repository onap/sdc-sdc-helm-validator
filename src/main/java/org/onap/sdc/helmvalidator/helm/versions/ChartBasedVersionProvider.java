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

import org.onap.sdc.helmvalidator.helm.versions.exception.NotSupportedApiVersionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChartBasedVersionProvider {

    private final SupportedVersionsProvider supportedVersionsProvider;
    private final ApiVersionsReader apiVersionsReader;

    @Autowired
    public ChartBasedVersionProvider(
        SupportedVersionsProvider supportedVersionsProvider,
        ApiVersionsReader apiVersionsReader) {
        this.supportedVersionsProvider = supportedVersionsProvider;
        this.apiVersionsReader = apiVersionsReader;
    }

    public String getVersion(String chartPath) {
        String apiVersion = apiVersionsReader.readVersion(chartPath);
        return mapToChartVersion(apiVersion);
    }

    private String mapToChartVersion(String apiVersion) {
        switch (apiVersion) {
            case "v1":
                return supportedVersionsProvider.getLatestVersion("2");
            case "v2":
                return supportedVersionsProvider.getLatestVersion("3");
            default:
                throw new NotSupportedApiVersionException("Cannot obtain Helm version from API version: " + apiVersion);
        }
    }

}
