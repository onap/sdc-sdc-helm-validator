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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.onap.sdc.helmvalidator.helm.versions.SupportedVersionsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SupportedVersionsController {

    private final SupportedVersionsProvider versionsProvider;

    @Autowired
    public SupportedVersionsController(SupportedVersionsProvider provider) {
        this.versionsProvider = provider;
    }

    @GetMapping("/versions")
    public ResponseEntity<Map<String, List<String>>> supportedVersions() {
        return mapVersionsToResponseEntity(versionsProvider.getVersions());
    }

    private ResponseEntity<Map<String, List<String>>> mapVersionsToResponseEntity(List<String> versions) {

        return new ResponseEntity<>(Collections.singletonMap("versions", versions), HttpStatus.OK);
    }
}
