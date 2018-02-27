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

package ru.skoltech.cedl.dataexchange.entity;

/**
 * Marker interface for all JPA entities, which contains a Hibernate Envers revision field.
 * Provide getter and setter for this field.
 * <p>
 * Created by Nikolay Groshkov on 19-Feb-18.
 */
public interface RevisedEntity {

    /**
     * Retrieve a revision number of current entity.
     *
     * @return current revision number
     */
    int getRevision();

    /**
     * Update a revision number of current entity.
     *
     * @param revision new revision number
     */
    void setRevision(int revision);

    /**
     * Update a revision number of current entity to maximum value.
     * This operation must be performed for prioritizing local updates on remote ones.
     */
    default void prioritizeRevision() {
        this.setRevision(Integer.MAX_VALUE);
    }

}
