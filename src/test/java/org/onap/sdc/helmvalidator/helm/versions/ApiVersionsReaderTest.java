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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.sdc.helmvalidator.helm.versions.exception.ApiVersionNotFoundException;
import org.onap.sdc.helmvalidator.helm.versions.exception.ReadFileException;

class ApiVersionsReaderTest {

    private static final String API_VERSION_V2 = "v2";
    private static final String TEST_RESOURCES_TMP = "src/test/resources/tmp";
    private static final Path TEST_CHART_PATH = Path.of(TEST_RESOURCES_TMP).resolve(Path.of("Chart.yaml"));

    private static final Path TEST_TAR_PATH = Path.of(TEST_RESOURCES_TMP, "test.tar");

    private ApiVersionsReader apiVersionsReader;

    @BeforeEach
    void setUp() {
        apiVersionsReader = new ApiVersionsReader();
    }

    @Test
    void shouldCorrectlyReadApiVersionFromTar() throws IOException {
        prepareTestTar(API_VERSION_V2);

        String helmVersion = apiVersionsReader.readVersion(TEST_TAR_PATH.toString());

        assertThat(helmVersion).isEqualTo(API_VERSION_V2);
    }

    @Test
    void shouldThrowExceptionWhenApiVersionIsNotProvided() throws IOException {
        prepareTestTar(null);
        final String chartPath = TEST_TAR_PATH.toString();
        Exception exception = assertThrows(ApiVersionNotFoundException.class,
            () -> apiVersionsReader.readVersion(chartPath));

        assertThat(exception).hasMessageContaining("Cannot find apiVersion value in a main chart");
    }

    @Test
    void shouldThrowExceptionForNotExistingPath() {
        String notExistingChartPath = "not/exiting/chart/path";

        Exception exception = assertThrows(ReadFileException.class,
            () -> apiVersionsReader.readVersion(notExistingChartPath));

        assertThat(exception).hasMessageContaining("Cannot read tar from path: " + notExistingChartPath);
    }

    @AfterEach
    void cleanTmpDir() throws IOException {
        Files.walk(Paths.get(TEST_RESOURCES_TMP))
            .map(Path::toFile)
            .filter(File::isFile)
            .forEach(File::delete);
    }

    private void prepareTestTar(String apiVersion) throws IOException {
        createTestChart(apiVersion);
        createTestTar();
    }

    private void createTestTar() throws IOException {
        TarArchiveOutputStream tarOutput = null;
        try {
            tarOutput = new TarArchiveOutputStream(
                new GzipCompressorOutputStream(new FileOutputStream(String.valueOf(TEST_TAR_PATH))));

            final String tarChartPath = "test/Chart.yaml";
            TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(TEST_CHART_PATH.toFile(), tarChartPath);
            tarOutput.putArchiveEntry(tarArchiveEntry);

            Files.copy(TEST_CHART_PATH, tarOutput);
        } finally {
            if (tarOutput != null) {
                tarOutput.closeArchiveEntry();
                tarOutput.close();
            }
        }
    }

    private void createTestChart(String apiVersion) throws IOException {
        String apiVersionLine = apiVersion != null ? "apiVersion: " + apiVersion : "";

        Files.createDirectories(TEST_CHART_PATH.getParent());
        Files.createFile(TEST_CHART_PATH);
        Files.write(TEST_CHART_PATH, List.of("appVersion: 1.0", apiVersionLine, "name: test-chart"));
    }

}
