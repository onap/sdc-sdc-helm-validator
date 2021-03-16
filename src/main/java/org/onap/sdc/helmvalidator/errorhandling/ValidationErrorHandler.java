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

package org.onap.sdc.helmvalidator.errorhandling;

import org.onap.sdc.helmvalidator.api.ValidationController;
import org.onap.sdc.helmvalidator.helm.validation.exception.BashExecutionException;
import org.onap.sdc.helmvalidator.helm.validation.exception.NotSupportedVersionException;
import org.onap.sdc.helmvalidator.helm.validation.exception.SaveFileException;
import org.onap.sdc.helmvalidator.helm.versions.exception.ApiVersionNotFoundException;
import org.onap.sdc.helmvalidator.helm.versions.exception.NotSupportedApiVersionException;
import org.onap.sdc.helmvalidator.helm.versions.exception.ReadFileException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ValidationController.class)
public class ValidationErrorHandler {

    /**
     * BashExecutionException handler.
     *
     * @param exception Exception that occurs during execution of bash command
     * @return ResponseEntity with ValidationErrorResponse created from given exception
     */
    @ExceptionHandler(value = BashExecutionException.class)
    public ResponseEntity<ValidationErrorResponse> handle(BashExecutionException exception) {
        return getErrorResponseEntity(
            exception.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * SaveFileException handler.
     *
     * @param exception Exception that occurs during file saving
     * @return ResponseEntity with ValidationErrorResponse created from given exception
     */
    @ExceptionHandler(value = SaveFileException.class)
    public ResponseEntity<ValidationErrorResponse> handle(SaveFileException exception) {
        return getErrorResponseEntity(
            exception.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * NotSupportedVersionException handler.
     *
     * @param exception Exception that occurs when not supported Helm version is requested for validation
     * @return ResponseEntity with ValidationErrorResponse created from given exception
     */
    @ExceptionHandler(value = NotSupportedVersionException.class)
    public ResponseEntity<ValidationErrorResponse> handle(NotSupportedVersionException exception) {
        return getErrorResponseEntity(
            exception.getMessage(),
            HttpStatus.BAD_REQUEST
        );
    }

    /**
     * ApiVersionNotFoundException handler.
     *
     * @param exception Exception that occurs when API version cannot be derived from Helm chart
     * @return ResponseEntity with ValidationErrorResponse created from given exception
     */
    @ExceptionHandler(value = ApiVersionNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handle(ApiVersionNotFoundException exception) {
        return getErrorResponseEntity(
            exception.getMessage(),
            HttpStatus.BAD_REQUEST
        );
    }

    /**
     * NotSupportedApiVersionException handler.
     *
     * @param exception Exception that occurs when API version from Helm chart is not supported
     * @return ResponseEntity with ValidationErrorResponse created from given exception
     */
    @ExceptionHandler(value = NotSupportedApiVersionException.class)
    public ResponseEntity<ValidationErrorResponse> handle(NotSupportedApiVersionException exception) {
        return getErrorResponseEntity(
            exception.getMessage(),
            HttpStatus.BAD_REQUEST
        );
    }

    /**
     * ReadFileException handler.
     *
     * @param exception Exception that occurs during reading of Helm chart
     * @return ResponseEntity with ValidationErrorResponse created from given exception
     */
    @ExceptionHandler(value = ReadFileException.class)
    public ResponseEntity<ValidationErrorResponse> handle(ReadFileException exception) {
        return getErrorResponseEntity(
            exception.getMessage(),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<ValidationErrorResponse> getErrorResponseEntity(String errorMessage, HttpStatus status) {
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(errorMessage);
        return new ResponseEntity<>(
            errorResponse,
            status
        );
    }
}
