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

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SemanticVersionComparatorTest {

    @ParameterizedTest
    @CsvSource({
        "3.14.4, 3.9.0",
        "3.10.0, 3.9.9",
        "3.5.10, 3.5.2",
        "4.0.0, 3.99.99",
        "3.5, 3.4.9"
    })
    void ordersNumericallyNotLexicographically(String higher, String lower) {
        assertThat(SemanticVersionComparator.INSTANCE.compare(higher, lower)).isPositive();
        assertThat(SemanticVersionComparator.INSTANCE.compare(lower, higher)).isNegative();
    }

    @Test
    void treatsMissingComponentAsZero() {
        assertThat(SemanticVersionComparator.INSTANCE.compare("3.5", "3.5.0")).isZero();
    }

    @Test
    void fallsBackToStringOrderForNonNumericComponent() {
        assertThat(SemanticVersionComparator.INSTANCE.compare("3.x.0", "3.y.0")).isNegative();
    }

    @Test
    void sortsDescendingWhenReversed() {
        List<String> versions = new ArrayList<>(List.of("3.9.0", "3.14.4", "2.17.0", "3.5.2"));

        versions.sort(SemanticVersionComparator.INSTANCE.reversed());

        assertThat(versions).containsExactly("3.14.4", "3.9.0", "3.5.2", "2.17.0");
    }
}
