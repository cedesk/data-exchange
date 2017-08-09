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

package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.entity.revision.CustomRevisionEntity;

/**
 * Custom Data Access Operations with {@link CustomRevisionEntity} entity.
 *
 * Created by Nikolay Groshkov on 07-Aug-17.
 */
public interface RevisionEntityRepositoryCustom {

    /**
     * Retrieve an instance of latest {@link CustomRevisionEntity} entity,
     * which was stored along with entity of specified class and id.
     *
     * @param id id of entity, which persistence produced required {@link CustomRevisionEntity}
     * @param entityClass class of entity, which persistence produced required {@link CustomRevisionEntity}
     * @return instance of {@link CustomRevisionEntity}
     */
    CustomRevisionEntity lastCustomRevisionEntity(Long id, Class entityClass);

}
