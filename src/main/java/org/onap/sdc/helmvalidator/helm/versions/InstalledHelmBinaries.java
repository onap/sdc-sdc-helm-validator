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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class InstalledHelmBinaries {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstalledHelmBinaries.class);
    private static final String PLAIN_HELM = "helm";
    private static final Pattern VERSIONED_HELM = Pattern.compile("^helm-v(\\d+\\.\\d+\\.\\d+)$");

    private final Supplier<String> pathSupplier;
    private final HelmVersionProbe probe;
    private Map<String, String> executablesByVersion;

    @Autowired
    public InstalledHelmBinaries() {
        this(() -> System.getenv("PATH"), new ProcessHelmVersionProbe());
    }

    public InstalledHelmBinaries(Supplier<String> pathSupplier, HelmVersionProbe probe) {
        this.pathSupplier = pathSupplier;
        this.probe = probe;
    }

    /**
     * Retrieves versions of all Helm binaries found on PATH.
     *
     * @return versions in PATH order
     */
    public Set<String> versions() {
        return executablesByVersion().keySet();
    }

    /**
     * Retrieves the executable that provides given Helm version.
     *
     * @param version Helm version as x.y.z
     * @return absolute path of the executable, or empty if no binary provides that version
     */
    public Optional<String> executableFor(String version) {
        return Optional.ofNullable(executablesByVersion().get(version));
    }

    /**
     * Scans once at startup, so the first health probe does not pay for it.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void scanOnStartup() {
        executablesByVersion();
    }

    private synchronized Map<String, String> executablesByVersion() {
        if (executablesByVersion == null) {
            executablesByVersion = Collections.unmodifiableMap(scan());
            LOGGER.info("Helm binaries found on PATH: {}", executablesByVersion);
        }
        return executablesByVersion;
    }

    private Map<String, String> scan() {
        Map<String, String> found = new LinkedHashMap<>();
        String path = pathSupplier.get();
        if (path == null) {
            return found;
        }
        for (String entry : path.split(File.pathSeparator)) {
            if (entry.isBlank()) {
                continue;
            }
            Path dir = Paths.get(entry);
            if (Files.isDirectory(dir)) {
                scanDirectory(dir, found);
            }
        }
        return found;
    }

    private void scanDirectory(Path dir, Map<String, String> found) {
        File[] files = dir.toFile().listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            Path candidate = file.toPath().toAbsolutePath();
            if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) {
                versionOf(candidate).ifPresent(version -> found.putIfAbsent(version, candidate.toString()));
            }
        }
    }

    private Optional<String> versionOf(Path candidate) {
        String name = candidate.getFileName().toString();
        Matcher versioned = VERSIONED_HELM.matcher(name);
        if (versioned.matches()) {
            return Optional.of(versioned.group(1));
        }
        if (PLAIN_HELM.equals(name)) {
            return probe.versionOf(candidate);
        }
        return Optional.empty();
    }
}
