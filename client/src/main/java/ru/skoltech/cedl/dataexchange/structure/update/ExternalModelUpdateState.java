/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.structure.update;

/**
 * Defines states of external model export update.
 * <p>
 * Created by Nikolay Groshkov on 07-Sep-17.
 */
public enum ExternalModelUpdateState {

    /**
     * State when export was successful
     */
    SUCCESS("Export successful"),

    /**
     * State when export is impossible because of empty export reference
     */
    FAIL_EMPTY_REFERENCE("Export is impossible because of empty export reference"),

    /**
     * State when export is impossible because of empty export reference external model
     */
    FAIL_EMPTY_REFERENCE_EXTERNAL_MODEL("Export is impossible because of empty export reference external model"),

    /**
     * State when export is impossible because of empty export reference target
     */
    FAIL_EMPTY_REFERENCE_TARGET("Export is impossible because of empty export reference target"),

    /**
     * State when export fails with an internal error
     */
    FAIL_EXPORT("Export failed with an internal error");

    final String description;

    ExternalModelUpdateState(String description) {
        this.description = description;
    }
}
