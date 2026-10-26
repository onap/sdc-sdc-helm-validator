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

import java.util.Comparator;

final class SemanticVersionComparator implements Comparator<String> {

    static final SemanticVersionComparator INSTANCE = new SemanticVersionComparator();

    private static final String SEPARATOR = "\\.";

    private SemanticVersionComparator() {
    }

    @Override
    public int compare(String first, String second) {
        String[] firstParts = first.split(SEPARATOR);
        String[] secondParts = second.split(SEPARATOR);
        int length = Math.max(firstParts.length, secondParts.length);
        for (int i = 0; i < length; i++) {
            String firstPart = i < firstParts.length ? firstParts[i] : "0";
            String secondPart = i < secondParts.length ? secondParts[i] : "0";
            int result = comparePart(firstPart, secondPart);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    private static int comparePart(String first, String second) {
        try {
            return Integer.compare(Integer.parseInt(first), Integer.parseInt(second));
        } catch (NumberFormatException e) {
            return first.compareTo(second);
        }
    }
}
