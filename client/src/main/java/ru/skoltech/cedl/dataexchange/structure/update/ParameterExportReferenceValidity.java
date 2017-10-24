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
 * Created by Nikolay Groshkov on 24-Oct-17.
 */
public enum ParameterExportReferenceValidity {

    /**
     * Empty export reference is empty
     */
    INVALID_EMPTY_REFERENCE("Empty export reference"),

    /**
     * Empty external model of export reference
     */
    INVALID_EMPTY_REFERENCE_EXTERNAL_MODEL("Empty external model of export reference"),

    /**
     * Empty target of export reference
     */
    INVALID_EMPTY_REFERENCE_TARGET("Empty target of export reference"),

    /**
     * Correct state
     */
    VALID("Valid");

    public final String description;

    ParameterExportReferenceValidity(String description) {
        this.description = description;
    }

    public boolean isValid(){
        return this == VALID;
    }
}
