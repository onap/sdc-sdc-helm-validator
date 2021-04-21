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

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition
public class DocsConfiguration {

    /**
     * Create Open API definition.
     *
     * @return Custom Open API definition
     */
    @Bean
    public OpenAPI customOpenApi() {
        var versionsSchema = new Schema<Map<String, List<String>>>().addProperties("versions", getArraySchema());

        return new OpenAPI().info(
            new Info()
                .title("OpenAPI definition for SDC Helm validator")
                .version("v0")
                .description("Application for validating Helm charts."))
            .components(new Components()
                .addSchemas("VersionsResponse", versionsSchema));
    }

    private ArraySchema getArraySchema() {
        ArraySchema arraySchema = new ArraySchema();
        arraySchema.setItems(new StringSchema());
        return arraySchema;
    }
}
