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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

@EnabledOnOs(OS.LINUX)
class InstalledHelmBinariesTest {

    private static final String REPORTS_3_19_0 =
        "#!/bin/sh\n[ \"$1\" = version ] && printf 'v3.19.0'\n";
    private static final String NOOP = "#!/bin/sh\nexit 0\n";

    @TempDir
    Path first;

    @TempDir
    Path second;

    @Test
    void findsVersionedBinariesByName() throws IOException {
        Path binary = executable(first, "helm-v3.5.2", NOOP);

        InstalledHelmBinaries binaries = binaries(first.toString());

        assertThat(binaries.versions()).containsExactly("3.5.2");
        assertThat(binaries.executableFor("3.5.2")).contains(binary.toString());
    }

    @Test
    void asksPlainHelmForItsVersion() throws IOException {
        Path binary = executable(first, "helm", REPORTS_3_19_0);

        InstalledHelmBinaries binaries = binaries(first.toString());

        assertThat(binaries.versions()).containsExactly("3.19.0");
        assertThat(binaries.executableFor("3.19.0")).contains(binary.toString());
    }

    @Test
    void firstPathEntryWinsForDuplicateVersion() throws IOException {
        Path versioned = executable(first, "helm-v3.19.0", NOOP);
        executable(second, "helm", REPORTS_3_19_0);

        InstalledHelmBinaries binaries = binaries(first + File.pathSeparator + second);

        assertThat(binaries.versions()).containsExactly("3.19.0");
        assertThat(binaries.executableFor("3.19.0")).contains(versioned.toString());
    }

    @Test
    void ignoresNonExecutableUnrelatedAndMalformedNames() throws IOException {
        Files.writeString(first.resolve("helm-v3.4.1"), NOOP);
        executable(first, "helm-vfoo", NOOP);
        executable(first, "helmfile", NOOP);
        executable(first, "helm-v3.3", NOOP);

        InstalledHelmBinaries binaries = binaries(first.toString());

        assertThat(binaries.versions()).isEmpty();
    }

    @Test
    void skipsPlainHelmWhoseProbeFails() throws IOException {
        executable(first, "helm", "#!/bin/sh\necho 'Error: could not find tiller'\nexit 1\n");

        assertThat(binaries(first.toString()).versions()).isEmpty();
    }

    @Test
    void probeTimesOut() throws IOException {
        Path hanging = executable(first, "helm", "#!/bin/sh\nsleep 30\n");

        long start = System.nanoTime();
        Optional<String> version = new ProcessHelmVersionProbe(Duration.ofMillis(500)).versionOf(hanging);

        assertThat(version).isEmpty();
        assertThat(Duration.ofNanos(System.nanoTime() - start)).isLessThan(Duration.ofSeconds(5));
    }

    @Test
    void toleratesMissingAndEmptyPathEntries() throws IOException {
        executable(first, "helm-v3.5.2", NOOP);

        InstalledHelmBinaries binaries = binaries(
            "/does/not/exist" + File.pathSeparator + File.pathSeparator + first);

        assertThat(binaries.versions()).containsExactly("3.5.2");
    }

    @Test
    void emptyWhenPathIsUnset() {
        assertThat(new InstalledHelmBinaries(() -> null, new ProcessHelmVersionProbe()).versions()).isEmpty();
    }

    @Test
    void scansOnlyOnce() throws IOException {
        executable(first, "helm", REPORTS_3_19_0);
        AtomicInteger probes = new AtomicInteger();
        HelmVersionProbe counting = executable -> {
            probes.incrementAndGet();
            return new ProcessHelmVersionProbe().versionOf(executable);
        };
        InstalledHelmBinaries binaries = new InstalledHelmBinaries(first::toString, counting);

        binaries.versions();
        binaries.executableFor("3.19.0");
        binaries.versions();

        assertThat(probes.get()).isEqualTo(1);
    }

    private static InstalledHelmBinaries binaries(String path) {
        return new InstalledHelmBinaries(() -> path, new ProcessHelmVersionProbe());
    }

    private static Path executable(Path dir, String name, String script) throws IOException {
        Path file = dir.resolve(name);
        Files.writeString(file, script);
        assertThat(file.toFile().setExecutable(true)).isTrue();
        return file;
    }
}
