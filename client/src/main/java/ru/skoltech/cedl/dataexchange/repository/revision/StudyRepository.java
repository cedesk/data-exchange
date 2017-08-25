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

package ru.skoltech.cedl.dataexchange.repository.revision;

import org.springframework.data.jpa.repository.Query;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.repository.custom.JpaRevisionEntityRepository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Data Access Operations with {@link Study} entity.
 * <p>
 * Created by Nikolay Groshkov on 06-Aug-17.
 */
public interface StudyRepository extends JpaRevisionEntityRepository<Study, Long> {

    /**
     * Remove from the database a {@link Study} with a specified name.
     *
     * @param name name of the {@link Study}
     */
    @Transactional
    void deleteByName(String name);

    /**
     * Retrieve names of all stored studies.
     *
     * @return list of studies names
     */
    @Query("SELECT name FROM Study")
    List<String> findAllNames();

    /**
     * Retrieve a {@link Study} by name.
     *
     * @param name name of the {@link Study}
     * @return instance of the {@link Study}
     */
    Study findByName(String name);

}
