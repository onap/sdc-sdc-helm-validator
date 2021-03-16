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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import org.onap.sdc.helmvalidator.helm.validation.exception.SaveFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileManager.class);
    private final String basePath;

    @Autowired
    FileManager(@Value("${app.config.charts-base-path}") String basePath) {
        this.basePath = basePath;
    }

    String saveFile(MultipartFile file) {
        LOGGER.debug("Base PATH: {}", basePath);
        try {
            String filePath = basePath + File.separator + generateFileName(file.getOriginalFilename());
            LOGGER.info("Attempt to save file : {}", filePath);
            Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
            return filePath;
        } catch (IOException e) {
            throw new SaveFileException("Cannot save file: " + file.getOriginalFilename(), e);
        }
    }

    void removeFile(String path) {
        try {
            LOGGER.debug("Attempt to delete file : {}", path);
            Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            LOGGER.warn("Cannot delete file: {}, Exception: {}", path, e.getStackTrace());
        }
    }

    private String generateFileName(String fileName) {
        return Instant.now().toEpochMilli() + "_" + fileName;
    }
}
