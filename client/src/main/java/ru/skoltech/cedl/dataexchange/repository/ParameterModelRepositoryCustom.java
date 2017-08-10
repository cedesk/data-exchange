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

import org.springframework.transaction.annotation.Transactional;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterRevision;

import java.util.List;

/**
 * Custom Data Access Operations with {@link ParameterModel} entity.
 *
 * Created by Nikolay Groshkov on 07-Aug-17.
 */
public interface ParameterModelRepositoryCustom {

    /**
     * Find all revisions of {@link ParameterModel} by specified id,
     * sorted by revision number in descending order.
     *
     * @param id of {@link ParameterModel}
     * @return List of revisions in array of {@link Object} format ({@link Object[]}
     */
    @Transactional(readOnly = true)
    List<ParameterRevision> findRevisionsOrderByRevisionNumberDesc(Long id);
}
