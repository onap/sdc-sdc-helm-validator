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

package org.onap.sdc.helmvalidator.helm.versions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessHelmVersionProbe implements HelmVersionProbe {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessHelmVersionProbe.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final Pattern VERSION = Pattern.compile("^v?(\\d+\\.\\d+\\.\\d+)$");

    private final Duration timeout;

    public ProcessHelmVersionProbe() {
        this(DEFAULT_TIMEOUT);
    }

    ProcessHelmVersionProbe(Duration timeout) {
        this.timeout = timeout;
    }

    @Override
    public Optional<String> versionOf(Path executable) {
        Process process = null;
        try {
            process = new ProcessBuilder(executable.toString(), "version", "--template", "{{.Version}}")
                .redirectErrorStream(true)
                .start();
            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                LOGGER.warn("Ignoring {}: no version reported within {}", executable, timeout);
                return Optional.empty();
            }
            String output = readAll(process.getInputStream()).trim();
            Matcher matcher = VERSION.matcher(output);
            if (process.exitValue() != 0 || !matcher.matches()) {
                LOGGER.warn("Ignoring {}: unexpected version output '{}'", executable, output);
                return Optional.empty();
            }
            return Optional.of(matcher.group(1));
        } catch (IOException e) {
            LOGGER.warn("Ignoring {}: {}", executable, e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    private static String readAll(InputStream stream) throws IOException {
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
}
