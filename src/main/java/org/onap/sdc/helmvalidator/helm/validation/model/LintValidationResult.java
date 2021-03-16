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

import java.util.List;

public class LintValidationResult {

    private final boolean isValid;

    private final List<String> lintErrors;
    private final List<String> lintWarnings;

    /**
     * Validation result of linting a Helm chart.
     * @param isValid flag indicating if chart is valid
     * @param lintErrors list of errors occurred during linting
     * @param lintWarnings list of warning occurred during linting
     */
    public LintValidationResult(boolean isValid, List<String> lintErrors, List<String> lintWarnings) {
        this.isValid = isValid;
        this.lintErrors = lintErrors;
        this.lintWarnings = lintWarnings;
    }

    boolean isValid() {
        return isValid;
    }

    List<String> getLintErrors() {
        return lintErrors;
    }

    List<String> getLintWarnings() {
        return lintWarnings;
    }
}
