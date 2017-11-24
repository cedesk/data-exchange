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
import ru.skoltech.cedl.dataexchange.entity.Component;
import ru.skoltech.cedl.dataexchange.repository.custom.JpaRevisionEntityRepository;

import java.util.List;

/**
 * Data Access Operations with {@link Component} entity.
 * <p>
 * Created by Nikolay Groshkov on 17-Nov-17.
 */
public interface ComponentRepository extends JpaRevisionEntityRepository<Component, Long> {

    /**
     * Retrieve all {@link Component}s for the specified category.
     *
     * @param category name of the {@link Component}
     * @return list of components
     */
    List<Component> findAllByCategory(String category);

    /**
     * Retrieve all component categories.
     *
     * @return list of component categories
     */
    @Query("SELECT DISTINCT category FROM Component order by category")
    List<String> findAllCategories();
}
