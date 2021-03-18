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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.onap.sdc.helmvalidator.helm.validation.exception.BashExecutionException;
import org.onap.sdc.helmvalidator.helm.validation.exception.NotSupportedVersionException;
import org.onap.sdc.helmvalidator.helm.validation.model.BashOutput;
import org.onap.sdc.helmvalidator.helm.validation.model.LintValidationResult;
import org.onap.sdc.helmvalidator.helm.validation.model.TemplateValidationResult;
import org.onap.sdc.helmvalidator.helm.validation.model.ValidationResult;
import org.onap.sdc.helmvalidator.helm.versions.ChartBasedVersionProvider;
import org.onap.sdc.helmvalidator.helm.versions.SupportedVersionsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationService.class);
    private static final String TEMPLATE_OPTION = "template";
    private static final String LINT_OPTION = "lint";
    private static final String HELM_SUMMARY_MESSAGE_PATTERN =
        "Error: \\d* chart\\(s\\) linted, \\d* chart\\(s\\) failed";
    private static final boolean INVALID_RESULT = false;

    private final FileManager fileManager;

    private final BashExecutor executor;

    private final SupportedVersionsProvider supportedVersionsProvider;

    private final ChartBasedVersionProvider chartBasedVersionProvider;


    /**
     * Constructor for ValidationService.
     *
     * @param fileManager               object responsible for file manging
     * @param executor                  object responsible for running shell commands
     * @param supportedVersionsProvider object providing supported versions of Helm
     * @param chartBasedVersionProvider object allowing to derive Helm version from a chart
     */
    @Autowired
    public ValidationService(
        FileManager fileManager, BashExecutor executor,
        SupportedVersionsProvider supportedVersionsProvider,
        ChartBasedVersionProvider chartBasedVersionProvider) {
        this.fileManager = fileManager;
        this.executor = executor;
        this.supportedVersionsProvider = supportedVersionsProvider;
        this.chartBasedVersionProvider = chartBasedVersionProvider;
    }

    /**
     * Process Helm chart package with given options.
     *
     * @param desiredVersion requested version of Helm client to be used
     * @param file           packaged Helm chart file
     * @param isLinted       flag deciding if chart should be linted
     * @param isStrictLinted flag deciding if chart should be linted with strict option turned on
     * @return Result of Helm chart validation
     */
    public ValidationResult process(String desiredVersion, MultipartFile file, boolean isLinted,
        boolean isStrictLinted) {
        String chartPath = fileManager.saveFile(file);
        try {
            String helmVersion = getSupportedHelmVersion(desiredVersion, chartPath);
            return validateChart(helmVersion, file, isLinted, isStrictLinted, chartPath);
        } finally {
            LOGGER.info("File process finished");
            fileManager.removeFile(chartPath);
        }
    }

    private String getSupportedHelmVersion(String desiredVersion, String chartPath) {
        if (desiredVersion == null) {
            return chartBasedVersionProvider.getVersion(chartPath);
        }

        if (desiredVersion.startsWith("v")) {
            String helmMajorVersion = desiredVersion.substring(1);
            return supportedVersionsProvider.getLatestVersion(helmMajorVersion);
        }

        return supportedVersionsProvider.getVersions()
            .stream()
            .filter(s -> s.equals(desiredVersion))
            .findFirst()
            .orElseThrow(() -> new NotSupportedVersionException(desiredVersion));
    }

    private ValidationResult validateChart(String version, MultipartFile file, boolean isLinted, boolean isStrictLinted,
        String chartPath) {
        LOGGER.info("Start validation of file: {}, with helm version: {}",
            file.getOriginalFilename(), version);

        TemplateValidationResult templateValidationResult = runHelmTemplate(
            buildHelmTemplateCommand(version, chartPath));
        LOGGER.info("Helm template finished");

        if (isLinted) {
            LOGGER.info("Start helm lint, strict: {}", isStrictLinted);
            LintValidationResult lintValidationResult = runHelmLint(
                buildHelmLintCommand(version, chartPath, isStrictLinted));
            LOGGER.info("Helm lint finished");
            return new ValidationResult(templateValidationResult, lintValidationResult, version);
        }

        return new ValidationResult(templateValidationResult, version);
    }


    private String buildHelmTemplateCommand(String version, String chartPath) {
        return "helm-v" + version + " " + TEMPLATE_OPTION + " " + chartPath;
    }

    private TemplateValidationResult runHelmTemplate(String helmCommand)
        throws BashExecutionException {

        LOGGER.debug("Command executions: {} ", helmCommand);
        BashOutput chartTemplateResult = executor.execute(helmCommand);
        LOGGER.debug("Status code: {}", chartTemplateResult.getExitValue());
        if (chartTemplateResult.getExitValue() != 0) {
            List<String> renderingErrors = parseTemplateError(chartTemplateResult.getOutputLines());
            return new TemplateValidationResult(false, renderingErrors);
        }
        return new TemplateValidationResult(true, Collections.emptyList());
    }

    private String buildHelmLintCommand(String version, String chartPath, boolean isStrictLint) {
        String command = "helm-v" + version + " " + LINT_OPTION + " " + chartPath;
        if (isStrictLint) {
            return command + " --strict";
        }
        return command;
    }

    private LintValidationResult runHelmLint(String helmCommand) {
        BashOutput chartLintResult = executor.execute(helmCommand);

        List<String> lintErrors = parseLintError(chartLintResult.getOutputLines());
        List<String> lintWarnings = parseWarningError(chartLintResult.getOutputLines());

        boolean isSuccessExitStatus = isSuccessExitStatus(chartLintResult.getExitValue());

        if (isInvalidWithoutStandardError(isSuccessExitStatus, lintErrors, lintWarnings)) {
            return new LintValidationResult(INVALID_RESULT, chartLintResult.getOutputLines(), new ArrayList<>());
        }

        return new LintValidationResult(isSuccessExitStatus, lintErrors, lintWarnings);
    }

    private boolean isInvalidWithoutStandardError(boolean isValid, List<String> lintErrors, List<String> lintWarnings) {
        return !isValid && lintErrors.isEmpty() && lintWarnings.isEmpty();
    }

    private boolean isSuccessExitStatus(int exitValue) {
        return exitValue == 0;
    }

    private List<String> parseTemplateError(List<String> outputLines) {

        return outputLines.stream()
            .filter(s -> s.startsWith("Error:"))
            .collect(Collectors.toList());
    }

    private List<String> parseWarningError(List<String> outputLines) {
        return outputLines.stream()
            .filter(s -> s.startsWith("[WARNING]"))
            .collect(Collectors.toList());
    }

    private List<String> parseLintError(List<String> outputLines) {
        return outputLines.stream()
            .filter(s -> s.startsWith("[ERROR]") || s.startsWith("Error"))
            .filter(this::isNotHelmSummaryMessage)
            .collect(Collectors.toList());
    }

    private boolean isNotHelmSummaryMessage(String line) {
        Pattern pattern = Pattern.compile(HELM_SUMMARY_MESSAGE_PATTERN);
        return !pattern.matcher(line).matches();
    }
}
