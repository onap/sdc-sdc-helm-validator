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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import org.onap.sdc.helmvalidator.helm.validation.exception.BashExecutionException;
import org.onap.sdc.helmvalidator.helm.validation.model.BashOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BashExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        BashExecutor.class);

    BashOutput execute(String helmCommand) {

        try {
            ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", helmCommand);
            pb.redirectErrorStream(true);
            LOGGER.debug("Start process");
            Process process = pb.start();

            List<String> processOutput = readOutputAndCloseProcess(process);
            return new BashOutput(process.exitValue(), processOutput);
        } catch (IOException e) {
            throw new BashExecutionException("Error during bash execution: ", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BashExecutionException("Bash execution interrupted, error: ", e);
        }
    }

    private List<String> readOutputAndCloseProcess(Process process) throws IOException, InterruptedException {

        final InputStream inputStream = process.getInputStream();
        final List<String> lines = new BufferedReader(new InputStreamReader(inputStream))
            .lines().collect(Collectors.toList());

        // For compatibility with Helm2 and Helm3
        process.waitFor();
        inputStream.close();
        process.destroy();

        return lines;
    }
}
