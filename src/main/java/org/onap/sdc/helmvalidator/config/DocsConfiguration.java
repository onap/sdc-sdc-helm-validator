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

package org.onap.sdc.helmvalidator.config;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.onap.sdc.helmvalidator.helm.validation.model.LintValidationResult;
import org.onap.sdc.helmvalidator.helm.validation.model.TemplateValidationResult;
import org.onap.sdc.helmvalidator.helm.validation.model.ValidationResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition
public class DocsConfiguration {

    private static final String EXAMPLE_VERSION = "3.5.2";
    private static final ValidationResult SIMPLE_VALIDATION_RESPONSE_OBJECT = new ValidationResult(
        new TemplateValidationResult(true, Collections.emptyList()),
        EXAMPLE_VERSION);
    private static final ValidationResult LINT_VALIDATION_RESPONSE_OBJECT = new ValidationResult(
        new TemplateValidationResult(true, Collections.emptyList()),
        new LintValidationResult(true, Collections.emptyList(), Collections.emptyList()),
        EXAMPLE_VERSION);

    /**
     * Create Open API definition.
     *
     * @return Custom Open API definition
     */
    @Bean
    public OpenAPI customOpenApi() throws JsonProcessingException {
        var versionsSchema = new Schema<Map<String, List<String>>>().addProperties("versions", getArraySchema());

        return new OpenAPI().info(
            new Info()
                .title("OpenAPI definition for SDC Helm validator")
                .version("v0")
                .description("Application for validating Helm charts."))
            .components(new Components()
                .addSchemas("VersionsResponse", versionsSchema)
                .addExamples("simpleValidation", getSimpleExample())
                .addExamples("validationWithLint", getLintExample())
            );
    }

    private ArraySchema getArraySchema() {
        var arraySchema = new ArraySchema();
        arraySchema.setItems(new StringSchema());
        return arraySchema;
    }

    private Example getSimpleExample() throws JsonProcessingException {
        var simpleExample = new Example();
        simpleExample.setValue(getJsonResponse(SIMPLE_VALIDATION_RESPONSE_OBJECT));
        simpleExample.setDescription("Example response when parameter isLinted is set to false");
        return simpleExample;
    }

    private Example getLintExample() throws JsonProcessingException {
        var lintExample = new Example();
        lintExample.setValue(getJsonResponse(LINT_VALIDATION_RESPONSE_OBJECT));
        lintExample.setDescription("Example response when parameter isLinted is set to true");
        return lintExample;
    }

    private String getJsonResponse(ValidationResult validationResult) throws JsonProcessingException {
        ObjectMapper mapper = getObjectMapper();
        return mapper.writeValueAsString(validationResult);
    }

    private ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper;
    }
}
