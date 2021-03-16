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

package org.onap.sdc.helmvalidator.helm.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.sdc.helmvalidator.helm.validation.exception.BashExecutionException;
import org.onap.sdc.helmvalidator.helm.validation.exception.NotSupportedVersionException;
import org.onap.sdc.helmvalidator.helm.validation.exception.SaveFileException;
import org.onap.sdc.helmvalidator.helm.validation.model.BashOutput;
import org.onap.sdc.helmvalidator.helm.validation.model.ValidationResult;
import org.onap.sdc.helmvalidator.helm.versions.ChartBasedVersionProvider;
import org.onap.sdc.helmvalidator.helm.versions.SupportedVersionsProvider;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    private static final String HELM_ERROR_ON_TEMPLATE =
        "Error: parse error at (sample/templates/fail.yaml:1): function \"deliberateSyntaxError\" not defined";
    private static final String HELM_ERROR_ON_LINT =
        "[ERROR] templates/: parse error at (sample/templates/fail.yaml:1): function \"deliberateSyntaxError\" "
            + "not defined\n";
    private static final String HELM_WARNING_ON_LINT =
        "[WARNING] templates/: directory not found\n";
    private static final String HELM_WITHOUT_STANDARD_ERROR_ON_LINT =
        "unable to check Chart.yaml file in chart: stat /charts/1610528407457_apiv2.tar.gz/Chart.yaml: not a directory";
    private static final String HELM_LINT_ERROR_SUMMARY_MESSAGE = "Error: 0 chart(s) linted, 1 chart(s) failed";
    private static final String HELM_EMPTY_OUTPUT = "";

    private static final boolean LINTED = true;
    private static final boolean NOT_LINTED = false;
    private static final boolean STRICT_LINTED = true;
    private static final boolean NOT_STRICT_LINTED = false;
    private static final int SUCCESS_HELM_EXIT_CODE = 0;
    private static final int UNSUCCESSFUL_HELM_EXIT_CODE = 1;

    private static final String SAMPLE_VERSION = "3.3.3";
    private static final List<String> SAMPLE_VERSIONS = List.of("3.3.4", "3.3.3", "2.3.1", "2.1.4", "2.3.4");
    private static final String NOT_SUPPORTED_VERSION = "not supported version";

    private static final String SAMPLE_PATH = "samplePath";
    private static final String HELM_TEMPLATE = "helm-v3.3.3 template samplePath";
    private static final String HELM_LINT = "helm-v3.3.3 lint samplePath";
    private static final String HELM_LINT_STRICT = "helm-v3.3.3 lint samplePath --strict";
    private static final int EXPECTED_ONE = 1;

    private ValidationService validationService;

    @Mock
    private FileManager fileManager;

    @Mock
    private SupportedVersionsProvider versionsProvider;

    @Mock
    private ChartBasedVersionProvider chartBasedProvider;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private BashExecutor bashExecutor;

    @BeforeEach
    void setUp() {
        when(fileManager.saveFile(multipartFile)).thenReturn(SAMPLE_PATH);
        lenient().when(versionsProvider.getVersions()).thenReturn(SAMPLE_VERSIONS);
        this.validationService = new ValidationService(fileManager, bashExecutor, versionsProvider, chartBasedProvider);
    }

    @Test
    void shouldThrowExceptionWhenCannotSaveFile() {
        when(fileManager.saveFile(multipartFile)).thenThrow(SaveFileException.class);

        assertThatExceptionOfType(SaveFileException.class)
            .isThrownBy(
                () -> validationService.process(SAMPLE_VERSION, multipartFile, LINTED, STRICT_LINTED));
    }

    @Test
    void shouldThrowExceptionWhenVersionsIsNotSupported() {

        assertThatExceptionOfType(NotSupportedVersionException.class)
            .isThrownBy(
                () -> validationService.process(NOT_SUPPORTED_VERSION, multipartFile, LINTED, STRICT_LINTED));
    }

    @Test
    void shouldBeValidAndDeployableForNoErrorsAndNoWarning() {
        mockBashCommand(HELM_TEMPLATE, SUCCESS_HELM_EXIT_CODE, HELM_EMPTY_OUTPUT);
        mockBashCommand(HELM_LINT_STRICT, SUCCESS_HELM_EXIT_CODE, HELM_EMPTY_OUTPUT);

        ValidationResult validationResult = validationService
            .process(SAMPLE_VERSION, multipartFile, LINTED, STRICT_LINTED);

        assertThat(validationResult.isDeployable()).isTrue();
        assertThat(validationResult.isValid()).isTrue();
        assertThat(validationResult.getRenderErrors()).isEmpty();
        assertThat(validationResult.getLintError()).isEmpty();
        assertThat(validationResult.getLintWarning()).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"v2", "v3"})
    void shouldBeDeployableForLatestHelmVersion(String desiredVersion) {
        final String majorVersion = desiredVersion.substring(1);
        final String helmVersion = getLatestSampleHelmVersion(majorVersion);
        final String helmTemplate = String.format("helm-v%s template samplePath", helmVersion);
        when(versionsProvider.getLatestVersion(Mockito.anyString())).thenReturn(helmVersion);
        mockBashCommand(helmTemplate, SUCCESS_HELM_EXIT_CODE, HELM_EMPTY_OUTPUT);

        ValidationResult validationResult = validationService
            .process(desiredVersion, multipartFile, NOT_LINTED, NOT_STRICT_LINTED);

        assertThat(validationResult.isDeployable()).isTrue();
        assertThat(validationResult.getRenderErrors()).isEmpty();
        verify(versionsProvider).getLatestVersion(majorVersion);
    }

    @Test
    void shouldBeValidAndDeployableForVersionTakenFromChart() {
        when(chartBasedProvider.getVersion(Mockito.anyString())).thenReturn(SAMPLE_VERSION);
        mockBashCommand(HELM_TEMPLATE, SUCCESS_HELM_EXIT_CODE, HELM_EMPTY_OUTPUT);
        mockBashCommand(HELM_LINT_STRICT, SUCCESS_HELM_EXIT_CODE, HELM_EMPTY_OUTPUT);

        ValidationResult validationResult = validationService
            .process(null, multipartFile, LINTED, STRICT_LINTED);

        assertThat(validationResult.isDeployable()).isTrue();
        assertThat(validationResult.isValid()).isTrue();
        assertThat(validationResult.getRenderErrors()).isEmpty();
        assertThat(validationResult.getLintError()).isEmpty();
        assertThat(validationResult.getLintWarning()).isEmpty();
        verify(chartBasedProvider).getVersion(SAMPLE_PATH);
    }

    @Test
    void shouldBeUndeployableForRenderErrorsWithMessages() {
        mockBashCommand(HELM_TEMPLATE, UNSUCCESSFUL_HELM_EXIT_CODE, HELM_ERROR_ON_TEMPLATE);

        ValidationResult validationResult = validationService
            .process(SAMPLE_VERSION, multipartFile, NOT_LINTED, NOT_STRICT_LINTED);

        assertThat(validationResult.isDeployable()).isFalse();
        assertThat(validationResult.getRenderErrors()).isNotEmpty();
    }

    @Test
    void shouldBeUndeployableAndValidWhenRenderFailAndLintSuccessfulWithMessages()
        throws BashExecutionException, SaveFileException {
        mockBashCommand(HELM_TEMPLATE, UNSUCCESSFUL_HELM_EXIT_CODE, HELM_ERROR_ON_TEMPLATE);
        mockBashCommand(HELM_LINT_STRICT, SUCCESS_HELM_EXIT_CODE, HELM_EMPTY_OUTPUT);

        ValidationResult validationResult = validationService
            .process(SAMPLE_VERSION, multipartFile, LINTED, STRICT_LINTED);

        assertThat(validationResult.isDeployable()).isFalse();
        assertThat(validationResult.isValid()).isTrue();
        assertThat(validationResult.getRenderErrors()).isNotEmpty();
        assertThat(validationResult.getLintError()).isEmpty();
        assertThat(validationResult.getLintWarning()).isEmpty();
    }

    @Test
    void shouldBeDeployableAndInvalidForErrorOnLintWithMessages() {
        mockBashCommand(HELM_TEMPLATE, SUCCESS_HELM_EXIT_CODE, HELM_EMPTY_OUTPUT);
        mockBashCommand(HELM_LINT, UNSUCCESSFUL_HELM_EXIT_CODE, HELM_ERROR_ON_LINT);

        ValidationResult validationResult = validationService
            .process(SAMPLE_VERSION, multipartFile, LINTED, NOT_STRICT_LINTED);

        assertThat(validationResult.isDeployable()).isTrue();
        assertThat(validationResult.isValid()).isFalse();
        assertThat(validationResult.getRenderErrors()).isEmpty();
        assertThat(validationResult.getLintError()).isNotEmpty();
        assertThat(validationResult.getLintWarning()).isEmpty();
    }

    @Test
    void shouldBeDeployableAndInvalidForWarningOnLintAndStrictLintWithMessages()
        throws BashExecutionException, SaveFileException {
        mockBashCommand(HELM_TEMPLATE, SUCCESS_HELM_EXIT_CODE, HELM_EMPTY_OUTPUT);
        mockBashCommand(HELM_LINT_STRICT, UNSUCCESSFUL_HELM_EXIT_CODE, HELM_WARNING_ON_LINT);

        ValidationResult validationResult = validationService
            .process(SAMPLE_VERSION, multipartFile, LINTED, STRICT_LINTED);

        assertThat(validationResult.isDeployable()).isTrue();
        assertThat(validationResult.isValid()).isFalse();
        assertThat(validationResult.getRenderErrors()).isEmpty();
        assertThat(validationResult.getLintError()).isEmpty();
        assertThat(validationResult.getLintWarning()).isNotEmpty();
    }

    @Test
    void shouldBeUndeployableAndInvalidForErrorOnTemplateAndErrorOnLintWithMessages()
        throws BashExecutionException, SaveFileException {
        mockBashCommand(HELM_TEMPLATE, UNSUCCESSFUL_HELM_EXIT_CODE, HELM_ERROR_ON_TEMPLATE);
        mockBashCommand(HELM_LINT_STRICT, UNSUCCESSFUL_HELM_EXIT_CODE, HELM_WARNING_ON_LINT, HELM_ERROR_ON_LINT);

        ValidationResult validationResult = validationService
            .process(SAMPLE_VERSION, multipartFile, LINTED, STRICT_LINTED);

        assertThat(validationResult.isDeployable()).isFalse();
        assertThat(validationResult.isValid()).isFalse();
        assertThat(validationResult.getRenderErrors()).isNotEmpty();
        assertThat(validationResult.getLintError()).isNotEmpty();
        assertThat(validationResult.getLintWarning()).isNotEmpty();
    }

    @Test
    void shouldBeInvalidForWarningOnStrictLintWithMessages() throws BashExecutionException, SaveFileException {
        mockBashCommand(HELM_TEMPLATE, SUCCESS_HELM_EXIT_CODE, HELM_EMPTY_OUTPUT);
        mockBashCommand(HELM_LINT_STRICT, UNSUCCESSFUL_HELM_EXIT_CODE, HELM_WARNING_ON_LINT);

        ValidationResult validationResult = validationService
            .process(SAMPLE_VERSION, multipartFile, LINTED, STRICT_LINTED);

        assertThat(validationResult.isValid()).isFalse();
        assertThat(validationResult.getLintError()).isEmpty();
        assertThat(validationResult.getLintWarning()).isNotEmpty();
    }

    @Test
    void shouldAddUsedVersionToValidationResultOnLinted() {
        mockBashCommand(HELM_TEMPLATE, SUCCESS_HELM_EXIT_CODE, HELM_EMPTY_OUTPUT);
        mockBashCommand(HELM_LINT_STRICT, SUCCESS_HELM_EXIT_CODE, HELM_EMPTY_OUTPUT);

        ValidationResult validationResult = validationService
            .process(SAMPLE_VERSION, multipartFile, LINTED, STRICT_LINTED);
        assertThat(validationResult.getVersionUsed()).isEqualTo(SAMPLE_VERSION);
    }

    @Test
    void shouldAddUsedVersionToValidationResultOnNotLinted() {
        mockBashCommand(HELM_TEMPLATE, SUCCESS_HELM_EXIT_CODE, HELM_EMPTY_OUTPUT);

        ValidationResult validationResult = validationService
            .process(SAMPLE_VERSION, multipartFile, NOT_LINTED, NOT_STRICT_LINTED);
        assertThat(validationResult.getVersionUsed()).isEqualTo(SAMPLE_VERSION);
    }

    @Test
    void shouldAddBashOutputToResultWhenHelmReturnNonStandardError() {
        mockBashCommand(HELM_TEMPLATE, SUCCESS_HELM_EXIT_CODE, HELM_EMPTY_OUTPUT);
        mockBashCommand(HELM_LINT_STRICT, UNSUCCESSFUL_HELM_EXIT_CODE, HELM_WITHOUT_STANDARD_ERROR_ON_LINT);

        ValidationResult validationResult = validationService
            .process(SAMPLE_VERSION, multipartFile, LINTED, STRICT_LINTED);
        assertThat(validationResult.getLintError()).isNotEmpty();
    }

    @Test
    void shouldNotAddSummaryMessageToLintErrors() {
        mockBashCommand(HELM_TEMPLATE, SUCCESS_HELM_EXIT_CODE, HELM_EMPTY_OUTPUT);
        mockBashCommand(HELM_LINT_STRICT, UNSUCCESSFUL_HELM_EXIT_CODE, HELM_ERROR_ON_LINT,
            HELM_LINT_ERROR_SUMMARY_MESSAGE);

        ValidationResult validationResult = validationService
            .process(SAMPLE_VERSION, multipartFile, LINTED, STRICT_LINTED);
        assertThat(validationResult.getLintError()).isNotEmpty();
        assertThat(validationResult.getLintError()).hasSize(EXPECTED_ONE);
    }

    private String getLatestSampleHelmVersion(String majorVersion) {
        return SAMPLE_VERSIONS.stream()
            .filter(version -> version.startsWith(majorVersion))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Not supported version"));
    }

    private void mockBashCommand(String command, int exitValue, String... consoleOutput) {
        when(bashExecutor.execute(command))
            .thenReturn(new BashOutput(exitValue, mockBashConsoleLog(consoleOutput)));
    }

    private List<String> mockBashConsoleLog(String... args) {
        return Arrays.stream(args).collect(Collectors.toList());
    }
}
