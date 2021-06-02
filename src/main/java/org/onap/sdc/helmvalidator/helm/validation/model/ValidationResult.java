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

package org.onap.sdc.helmvalidator.helm.validation.model;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ValidationResult {

    private final Boolean isDeployable;
    private final List<String> renderErrors;
    private final Boolean isValid;
    private final List<String> lintWarning;
    private final List<String> lintError;
    private final String versionUsed;


    /**
     * ValidationResult constructor when linting is enabled.
     * @param templateValidationResult result of helm chart templating
     * @param lintValidationResult result of helm chart linting
     * @param versionUsed version of helm client used
     */
    public ValidationResult(
        TemplateValidationResult templateValidationResult,
        LintValidationResult lintValidationResult,
        String versionUsed) {
        this.isDeployable = templateValidationResult.isDeployable();
        this.renderErrors = templateValidationResult.getRenderErrors();

        this.isValid = lintValidationResult.isValid();
        this.lintWarning = lintValidationResult.getLintWarnings();
        this.lintError = lintValidationResult.getLintErrors();
        this.versionUsed = versionUsed;
    }

    /**
     * ValidationResult constructor when linting is disabled.
     * @param templateValidationResult result of helm chart templating
     * @param versionUsed version of helm client used
     */
    public ValidationResult(
        TemplateValidationResult templateValidationResult, String versionUsed) {
        this.isDeployable = templateValidationResult.isDeployable();
        this.renderErrors = templateValidationResult.getRenderErrors();

        this.isValid = null;
        this.lintWarning = null;
        this.lintError = null;
        this.versionUsed = versionUsed;
    }

    public Boolean isDeployable() {
        return isDeployable;
    }

    public List<String> getRenderErrors() {
        return Optional.ofNullable(renderErrors)
            .map(Collections::unmodifiableList).orElse(null);
    }

    public Boolean isValid() {
        return isValid;
    }

    public List<String> getLintWarning() {
        return Optional.ofNullable(lintWarning)
            .map(Collections::unmodifiableList).orElse(null);
    }

    public List<String> getLintError() {
        return Optional.ofNullable(lintError)
            .map(Collections::unmodifiableList).orElse(null);
    }

    public String getVersionUsed() {
        return versionUsed;
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
            "isDeployable=" + isDeployable +
            ", renderErrors=" + renderErrors +
            ", isValid=" + isValid +
            ", lintWarning=" + lintWarning +
            ", lintError=" + lintError +
            ", versionUsed=" + versionUsed +
            "}";
    }
}
