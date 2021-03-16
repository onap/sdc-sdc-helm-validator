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

public class TemplateValidationResult {

    private final boolean isDeployable;

    private final List<String> renderErrors;

    /**
     * Validation result of templating a Helm chart.
     * @param isDeployable flag indicating if chart can be templated
     * @param renderErrors list of errors occurred during templating
     */
    public TemplateValidationResult(boolean isDeployable, List<String> renderErrors) {
        this.isDeployable = isDeployable;
        this.renderErrors = renderErrors;
    }

    boolean isDeployable() {
        return isDeployable;
    }

    List<String> getRenderErrors() {
        return renderErrors;
    }
}
