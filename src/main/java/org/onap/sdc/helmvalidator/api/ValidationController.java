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

package org.onap.sdc.helmvalidator.api;

import org.onap.sdc.helmvalidator.helm.validation.ValidationService;
import org.onap.sdc.helmvalidator.helm.validation.model.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ValidationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationController.class);

    private final ValidationService validationService;

    @Autowired
    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    /**
     * Validates Helm chart.
     *
     * @param version        requested version of Helm client to be used
     * @param file           packaged Helm chart file
     * @param isLinted       flag deciding if chart should be linted
     * @param isStrictLinted flag deciding if chart should be linted with strict option turned on
     * @return Response with result of validation
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validate(
        @RequestParam(value = "versionDesired", required = false) String version,
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "isLinted", required = false, defaultValue = "false") boolean isLinted,
        @RequestParam(value = "isStrictLinted", required = false, defaultValue = "false") boolean isStrictLinted) {
        LOGGER.debug("Received file: {}, size: {}, helm version: {}",
            file.getOriginalFilename(), file.getSize(), version);
        ValidationResult result = validationService
            .process(version, file, isLinted, isStrictLinted);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
