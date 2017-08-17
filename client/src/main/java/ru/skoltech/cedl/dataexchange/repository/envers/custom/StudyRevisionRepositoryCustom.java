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

package ru.skoltech.cedl.dataexchange.repository.envers.custom;

import org.springframework.transaction.annotation.Transactional;
import ru.skoltech.cedl.dataexchange.entity.Study;

/**
 * Custom Data Access Operations with {@link Study} revisions.
 *
 * Created by Nikolay Groshkov on 17-Aug-17.
 */
public interface StudyRevisionRepositoryCustom {

    /**
     * Find {@link Study} by revision number.
     *
     * @param id of a searched {@link Study}
     * @param revisionNumber a number of revision of searched {@link Study}
     * @return instance of searched {@link Study}
     */
    @Transactional(readOnly = true)
    Study findStudyByRevision(Long id, Integer revisionNumber);
}
