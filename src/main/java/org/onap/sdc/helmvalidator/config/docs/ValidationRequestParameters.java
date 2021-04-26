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


package org.onap.sdc.helmvalidator.config.docs;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema
public class ValidationRequestParameters {

    @Schema(description = "Desired Helm version which should be used to validate the chart. "
        + "If parameter is not provided validation is processing with version based on chart's apiVersion."
        + " Version could be provided in 'semantic version' or 'major version'.<br> "
        + "Allowed formats: <br>"
        + "- Semantic version [X.Y.Z] e.g. 3.5.2 <br>"
        + "- Major version [vX] e.g. v3")
    private String versionDesired;

    @Schema(description = "Helm chart that should be validated (packed in .tgz format)", required = true)
    private MultipartFile file;

    @Schema(description = "If true, there will be an attempt to lint chart")
    private Boolean isLinted;

    @Schema(description = "Strict linting marks the chart as invalid if detect any warning")
    private Boolean isStrictLinted;

    public String getVersionDesired() {
        return versionDesired;
    }

    public void setVersionDesired(String versionDesired) {
        this.versionDesired = versionDesired;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public boolean getIsLinted() {
        return isLinted;
    }

    public void setIsLinted(boolean linted) {
        isLinted = linted;
    }

    public boolean getIsStrictLinted() {
        return isStrictLinted;
    }

    public void setIsStrictLinted(boolean strictLinted) {
        isStrictLinted = strictLinted;
    }
}
