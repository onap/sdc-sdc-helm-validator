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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.onap.sdc.helmvalidator.errorhandling.ValidationErrorResponse;
import org.onap.sdc.helmvalidator.helm.versions.SupportedVersionsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "VersionsService")
public class SupportedVersionsController {

    private final SupportedVersionsProvider versionsProvider;

    @Autowired
    public SupportedVersionsController(SupportedVersionsProvider provider) {
        this.versionsProvider = provider;
    }

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Supported Helm versions successfully returned",
            content = @Content(schema = @Schema(ref = "#/components/schemas/VersionsResponse"))),
        @ApiResponse(responseCode = "500", description = "Something went wrong during getting Helm versions",
            content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class)))})
    @Operation(summary = "Show Helm versions", description = "Web endpoint for showing supported Helm versions.",
        tags = "VersionsService")
    @GetMapping("/versions")
    public ResponseEntity<Map<String, List<String>>> supportedVersions() {
        return mapVersionsToResponseEntity(versionsProvider.getVersions());
    }

    private ResponseEntity<Map<String, List<String>>> mapVersionsToResponseEntity(List<String> versions) {
        return new ResponseEntity<>(Collections.singletonMap("versions", versions), HttpStatus.OK);
    }
}
