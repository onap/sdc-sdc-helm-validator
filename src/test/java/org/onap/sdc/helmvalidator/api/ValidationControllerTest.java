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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.sdc.helmvalidator.helm.validation.ValidationService;
import org.onap.sdc.helmvalidator.helm.validation.exception.BashExecutionException;
import org.onap.sdc.helmvalidator.helm.validation.exception.NotSupportedVersionException;
import org.onap.sdc.helmvalidator.helm.validation.exception.SaveFileException;
import org.onap.sdc.helmvalidator.helm.validation.model.LintValidationResult;
import org.onap.sdc.helmvalidator.helm.validation.model.TemplateValidationResult;
import org.onap.sdc.helmvalidator.helm.validation.model.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class ValidationControllerTest {

    private static final String SAMPLE_VERSION = "3.3.4";
    private static final String VERSION_PARAM = "versionDesired";
    private static final String IS_LINTED_PARAM = "isLinted";
    private static final String IS_STRICT_LINTED_PARAM = "isStrictLinted";
    private static final String VALID = "valid";
    private static final String DEPLOYABLE = "deployable";
    private static final String RENDER_ERRORS = "renderErrors";
    private static final String LINT_WARNING = "lintWarning";
    private static final String LINT_ERROR = "lintError";
    private static final String SAMPLE_ORIGINAL_FILENAME = "sampleChart.tar.gz";
    private static final String FILE_KEY = "file";
    private static final String VALIDATION_ENDPOINT = "/validate";
    private static final String VERSION_USED = "versionUsed";

    private ValidationController validationController;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ValidationService validationService;

    @Mock
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        validationController = new ValidationController(validationService);
    }

    @Test
    void shouldReturnValidResponseForMockedFile() {
        TemplateValidationResult templateValidationResult = new TemplateValidationResult(true, new ArrayList<>());
        LintValidationResult lintValidationResult = new LintValidationResult(true, new ArrayList<>(),
            new ArrayList<>());

        when(validationService.process(SAMPLE_VERSION, multipartFile, true, true))
            .thenReturn(new ValidationResult(templateValidationResult, lintValidationResult, SAMPLE_VERSION));

        ResponseEntity<ValidationResult> result = validationController
            .validate(SAMPLE_VERSION, multipartFile, true, true);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().isDeployable()).isTrue();
        assertThat(result.getBody().isValid()).isTrue();
        assertThat(result.getBody().getRenderErrors()).isEmpty();
        assertThat(result.getBody().getLintError()).isEmpty();
        assertThat(result.getBody().getLintWarning()).isEmpty();
        assertThat(result.getBody().getVersionUsed()).isEqualTo(SAMPLE_VERSION);

    }

    @Test
    void shouldThrowExceptionWhenCannotSaveFile() {
        when(validationService.process(SAMPLE_VERSION, multipartFile, true, true)).thenThrow(SaveFileException.class);

        assertThatExceptionOfType(SaveFileException.class)
            .isThrownBy(() -> validationController.validate(SAMPLE_VERSION, multipartFile, true, true));
    }

    @Test
    void shouldThrowExceptionIfErrorOccursDuringBashExecution() {
        when(validationService.process(SAMPLE_VERSION, multipartFile, true, true))
            .thenThrow(BashExecutionException.class);

        assertThatExceptionOfType(BashExecutionException.class)
            .isThrownBy(() -> validationController.validate(SAMPLE_VERSION, multipartFile, true, true));
    }

    @Test
    void shouldThrowExceptionWhenProvidedVersionIsNotSupported() {
        when(validationService.process(SAMPLE_VERSION, multipartFile, true, true)).thenThrow(
            NotSupportedVersionException.class);

        assertThatExceptionOfType(NotSupportedVersionException.class)
            .isThrownBy(() -> validationController.validate(SAMPLE_VERSION, multipartFile, true, true));
    }

    @Test
    void shouldContainDeployAndValidParametersInResponseBodyIfLintedIsTrue() throws Exception {

        TemplateValidationResult templateValidationResult = new TemplateValidationResult(true, new ArrayList<>());
        LintValidationResult lintValidationResult = new LintValidationResult(true, new ArrayList<>(),
            new ArrayList<>());
        MockMultipartFile file = new MockMultipartFile(FILE_KEY, SAMPLE_ORIGINAL_FILENAME,
            MediaType.MULTIPART_FORM_DATA_VALUE, "test".getBytes());

        when(validationService.process(SAMPLE_VERSION, file, true, true))
            .thenReturn(new ValidationResult(templateValidationResult, lintValidationResult, SAMPLE_VERSION));

        MvcResult mvcResult = mockMvc.perform(
            multipart(VALIDATION_ENDPOINT)
                .file(file)
                .param(VERSION_PARAM, SAMPLE_VERSION)
                .param(IS_LINTED_PARAM, "true")
                .param(IS_STRICT_LINTED_PARAM, "true"))
            .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();

        assertThat(contentAsString).contains(VALID);
        assertThat(contentAsString).contains(DEPLOYABLE);
        assertThat(contentAsString).contains(RENDER_ERRORS);
        assertThat(contentAsString).contains(LINT_WARNING);
        assertThat(contentAsString).contains(LINT_ERROR);
        assertThat(contentAsString).contains(VERSION_USED);
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void shouldNotContainValidParametersInResponseBodyIfLintedIsFalse() throws Exception {

        TemplateValidationResult templateValidationResult = new TemplateValidationResult(true, new ArrayList<>());
        MockMultipartFile file = new MockMultipartFile(FILE_KEY, SAMPLE_ORIGINAL_FILENAME,
            MediaType.MULTIPART_FORM_DATA_VALUE, "test".getBytes());

        when(validationService.process(SAMPLE_VERSION, file, false, false))
            .thenReturn(new ValidationResult(templateValidationResult, SAMPLE_VERSION));

        MvcResult mvcResult = mockMvc.perform(
            multipart(VALIDATION_ENDPOINT)
                .file(file)
                .param(VERSION_PARAM, SAMPLE_VERSION)
                .param(IS_LINTED_PARAM, "false")
                .param(IS_STRICT_LINTED_PARAM, "false"))
            .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();

        assertThat(contentAsString).doesNotContain(VALID);
        assertThat(contentAsString).contains(DEPLOYABLE);
        assertThat(contentAsString).contains(RENDER_ERRORS);
        assertThat(contentAsString).doesNotContain(LINT_WARNING);
        assertThat(contentAsString).doesNotContain(LINT_ERROR);
        assertThat(contentAsString).contains(VERSION_USED);
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
    }
}
