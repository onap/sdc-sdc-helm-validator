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

package org.onap.sdc.helmvalidator.helm.versions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.onap.sdc.helmvalidator.helm.versions.exception.ApiVersionNotFoundException;
import org.onap.sdc.helmvalidator.helm.versions.exception.ReadFileException;
import org.springframework.stereotype.Service;

@Service
public class ApiVersionsReader {

    private static final int MAIN_CHART_DIR_DEPTH = 2;
    private static final String API_VERSION_PREFIX = "apiVersion:";
    private static final Path CHART_FILE_NAME = Path.of("Chart.yaml");

    String readVersion(String chartPath) {
        return tryReadVersionFromChart(chartPath)
            .orElseThrow(ApiVersionNotFoundException::new);
    }

    private Optional<String> tryReadVersionFromChart(String chartPath) {
        try (var tarInput = new TarArchiveInputStream(
            new GzipCompressorInputStream(new FileInputStream(chartPath)))) {
            return readVersionFromChart(tarInput);
        } catch (IOException e) {
            throw new ReadFileException("Cannot read tar from path: " + chartPath, e);
        }
    }

    private Optional<String> readVersionFromChart(TarArchiveInputStream tarInput) throws IOException {
        TarArchiveEntry currentEntry;
        while ((currentEntry = tarInput.getNextTarEntry()) != null) {
            if (isMainChartYaml(currentEntry)) {
                var bufferedReader = new BufferedReader(new InputStreamReader(tarInput));
                return bufferedReader.lines()
                    .filter(chartLine -> chartLine.contains(API_VERSION_PREFIX))
                    .map(apiVersionLine -> apiVersionLine.replace(API_VERSION_PREFIX, ""))
                    .map(String::trim)
                    .findFirst();
            }
        }
        return Optional.empty();
    }

    private boolean isMainChartYaml(TarArchiveEntry currentEntry) {
        var entryPath = Path.of(currentEntry.getName());
        return currentEntry.isFile()
            && CHART_FILE_NAME.equals(entryPath.getFileName())
            && (entryPath.getNameCount() == MAIN_CHART_DIR_DEPTH);
    }


}
