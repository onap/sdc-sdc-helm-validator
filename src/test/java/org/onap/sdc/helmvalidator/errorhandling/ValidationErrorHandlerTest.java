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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.sdc.helmvalidator.helm.validation.exception.BashExecutionException;
import org.onap.sdc.helmvalidator.helm.validation.exception.NotSupportedVersionException;
import org.onap.sdc.helmvalidator.helm.validation.exception.SaveFileException;
import org.onap.sdc.helmvalidator.helm.versions.exception.ApiVersionNotFoundException;
import org.onap.sdc.helmvalidator.helm.versions.exception.NotSupportedApiVersionException;
import org.onap.sdc.helmvalidator.helm.versions.exception.ReadFileException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ValidationErrorHandlerTest {

    private ValidationErrorHandler errorHandler;

    @Mock
    private Throwable cause;

    @BeforeEach
    void setUp() {
        errorHandler = new ValidationErrorHandler();
    }

    @Test
    void shouldReturnResponseEntityWithMessageWhenCannotSaveFile() {
        String expectedMessage = "Cannot save file test-chart.tar.gz";
        SaveFileException saveFileException = new SaveFileException(expectedMessage, cause);

        ResponseEntity<ValidationErrorResponse> responseEntity = errorHandler.handle(saveFileException);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void shouldReturnResponseEntityWithMessageWhenProvidedVersionIsNotSupported() {
        String version = "3.3.3";
        String expectedMessage = "Version: " + version + " is not supported";

        NotSupportedVersionException notSupportedVersionException = new NotSupportedVersionException(version);

        ResponseEntity<ValidationErrorResponse> responseEntity = errorHandler.handle(notSupportedVersionException);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    void shouldReturnResponseEntityWithMessageWhenErrorOccursDuringBashExecution() {
        String expectedMessage = "Error in bash executions";
        BashExecutionException bashExecutionException = new BashExecutionException(expectedMessage, cause);

        ResponseEntity<ValidationErrorResponse> responseEntity = errorHandler.handle(bashExecutionException);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(expectedMessage);

    }

    @Test
    void shouldReturnResponseEntityWithMessageWhenHelmApiVersionNotFoundInChart() {
        String expectedMessage = "Cannot find apiVersion value in a main chart";
        ApiVersionNotFoundException apiVersionNotFoundException = new ApiVersionNotFoundException();

        ResponseEntity<ValidationErrorResponse> responseEntity = errorHandler.handle(apiVersionNotFoundException);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(expectedMessage);

    }

    @Test
    void shouldReturnResponseEntityWithMessageWhenHelmApiVersionIsNotSupported() {
        String expectedMessage = "Cannot obtain Helm version from API version";
        NotSupportedApiVersionException notSupportedApiVersionException = new NotSupportedApiVersionException(
            expectedMessage);

        ResponseEntity<ValidationErrorResponse> responseEntity = errorHandler.handle(notSupportedApiVersionException);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(expectedMessage);

    }

    @Test
    void shouldReturnResponseEntityWithMessageWhenCannotReadChartFile() {
        String expectedMessage = "Cannot read tar file from path";
        ReadFileException readFileException = new ReadFileException(expectedMessage, cause);

        ResponseEntity<ValidationErrorResponse> responseEntity = errorHandler.handle(readFileException);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getBody().getMessage()).isEqualTo(expectedMessage);

    }

}
