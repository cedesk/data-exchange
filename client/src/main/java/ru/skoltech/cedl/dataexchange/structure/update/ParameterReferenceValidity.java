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
 * Defines a validity state of parameter model reference (either value or export).
 * <p>
 * Created by Nikolay Groshkov on 24-Oct-17.
 */
public enum ParameterReferenceValidity {

    /**
     * Empty external model of reference
     */
    INVALID_EMPTY_REFERENCE_EXTERNAL_MODEL("Empty external model of reference"),

    /**
     * Empty target of reference
     */
    INVALID_EMPTY_REFERENCE_TARGET("Empty target of reference"),

    /**
     * Correct state
     */
    VALID("Valid");

    /**
     * Validity state description
     */
    public final String description;

    ParameterReferenceValidity(String description) {
        this.description = description;
    }

    /**
     * Return <i>true<i/> if parameter model reference is valid ({@link ParameterReferenceValidity#VALID} state)
     * <p/>
     * @return <i>true<i/> if parameter model reference is valid, <i>false<i/> if opposite
     */
    public boolean isValid(){
        return this == VALID;
    }
}
