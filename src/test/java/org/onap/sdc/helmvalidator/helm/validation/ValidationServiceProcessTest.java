/*
 * ============LICENSE_START=======================================================
 * SDC-HELM-VALIDATOR
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.onap.sdc.helmvalidator.helm.validation.model.ValidationResult;
import org.onap.sdc.helmvalidator.helm.versions.ChartBasedVersionProvider;
import org.onap.sdc.helmvalidator.helm.versions.InstalledHelmBinaries;
import org.onap.sdc.helmvalidator.helm.versions.ProcessHelmVersionProbe;
import org.onap.sdc.helmvalidator.helm.versions.SupportedVersionsProvider;
import org.springframework.web.multipart.MultipartFile;

@EnabledOnOs(OS.LINUX)
class ValidationServiceProcessTest {

    private static final String FAKE_HELM = "#!/bin/sh\n"
        + "case \"$1\" in\n"
        + "  version) printf 'v3.19.0' ;;\n"
        + "  template) echo \"Error: template: rendered $2\"; exit 1 ;;\n"
        + "  lint) echo '[WARNING] templates/: directory not found'; exit 0 ;;\n"
        + "esac\n";

    @TempDir
    Path bin;

    @Test
    void runsTheDiscoveredBinaryForRealAndParsesItsOutput() throws IOException {
        Path helm = bin.resolve("helm");
        Files.writeString(helm, FAKE_HELM);
        assertThat(helm.toFile().setExecutable(true)).isTrue();

        InstalledHelmBinaries binaries = new InstalledHelmBinaries(bin::toString, new ProcessHelmVersionProbe());
        SupportedVersionsProvider versions = mock(SupportedVersionsProvider.class);
        when(versions.getVersions()).thenReturn(List.of("3.19.0"));
        FileManager fileManager = mock(FileManager.class);
        MultipartFile file = mock(MultipartFile.class);
        when(fileManager.saveFile(file)).thenReturn("/charts/sample.tgz");

        ValidationService service = new ValidationService(fileManager, new BashExecutor(), versions,
            mock(ChartBasedVersionProvider.class), binaries);

        ValidationResult result = service.process("3.19.0", file, true, false);

        assertThat(result.getVersionUsed()).isEqualTo("3.19.0");
        assertThat(result.isDeployable()).isFalse();
        assertThat(result.getRenderErrors()).containsExactly("Error: template: rendered /charts/sample.tgz");
        assertThat(result.isValid()).isTrue();
        assertThat(result.getLintWarning()).containsExactly("[WARNING] templates/: directory not found");
    }
}
