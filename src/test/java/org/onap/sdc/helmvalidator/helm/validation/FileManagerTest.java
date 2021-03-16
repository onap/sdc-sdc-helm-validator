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
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileManagerTest {

    private static final String TEST_RESOURCES_TMP = "src/test/resources/tmp";
    private static final File TEST_RESOURCES_DIR = new File(TEST_RESOURCES_TMP);
    private static final ByteArrayInputStream TEST_INPUT_STREAM = new ByteArrayInputStream("test".getBytes());
    private static final String SAMPLE_FILE_NAME = "sample_file";

    private FileManager fileManager;

    @Mock
    private MultipartFile multipartFile;

    @BeforeAll
    static void createTmpDir() {
        TEST_RESOURCES_DIR.mkdirs();
    }

    @AfterAll
    static void cleanTmpDir() throws IOException {
        Files.walk(Paths.get(TEST_RESOURCES_TMP))
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .filter(File::isFile)
            .forEach(File::delete);
        TEST_RESOURCES_DIR.delete();
    }

    @BeforeEach
    void setUp() {
        fileManager = new FileManager(TEST_RESOURCES_TMP);
    }

    @Test
    void saveMultipartFileAndReturnFilePath() throws IOException {
        mockMultipartFile();

        String filePath = fileManager.saveFile(multipartFile);

        assertThat(filePath).isNotBlank();
        assertThat(Files.exists(Paths.get(filePath))).isTrue();
    }


    @Test
    void removeFileByPath() throws IOException {
        mockMultipartFile();

        String filePath = fileManager.saveFile(multipartFile);
        fileManager.removeFile(filePath);

        assertThat(Files.exists(Paths.get(filePath))).isFalse();
    }

    private void mockMultipartFile() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn(SAMPLE_FILE_NAME);
        when(multipartFile.getInputStream()).thenReturn(TEST_INPUT_STREAM);
    }
}
