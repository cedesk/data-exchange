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
 * Defines states of parameter model updates.
 * <p>
 * Created by Nikolay Groshkov on 07-Sep-17.
 */
public enum ParameterModelUpdateState {

    /**
     * State when evaluation was successful
     */
    SUCCESS("Evaluation successful"),

    /**
     * State when evaluation was successful but the value stays unchanged
     */
    SUCCESS_WITHOUT_UPDATE("Evaluation successful with the same value"),

    /**
     * State when evaluation is impossible because of empty value reference
     */
    FAIL_EMPTY_REFERENCE("Evaluation is impossible because of empty value reference"),

    /**
     * State when evaluation is impossible because of empty value reference external model
     */
    FAIL_EMPTY_REFERENCE_EXTERNAL_MODEL("Evaluation is impossible because of empty value reference external model"),

    /**
     * State when evaluation is impossible because of empty value reference target
     */
    FAIL_EMPTY_REFERENCE_TARGET("Evaluation is impossible because of empty value reference target"),

    /**
     * State when evaluation produces an invalid value
     */
    FAIL_INVALID_VALUE("Evaluation produced an invalid value"),

    /**
     * State when evaluation fails with an internal error
     */
    FAIL_EVALUATION("Evaluation failed with an internal error");

    public final String description;

    ParameterModelUpdateState(String description) {
        this.description = description;
    }
}
