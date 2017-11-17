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

package ru.skoltech.cedl.dataexchange.service;

import ru.skoltech.cedl.dataexchange.entity.Component;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;

import java.util.List;

/**
 * Operations with model components.
 * <p>
 * Created by Nikolay Groshkov on 17-Nov-17.
 */
public interface ComponentService {

    /**
     * Create and save in the database new instance of {@link Component} based on the specified category and {@link ModelNode}.
     * The copy of the specified {@link ModelNode} will be provided for the component.
     * <p/>
     *
     * @param category  category of the component
     * @param modelNode model node to link to
     * @return an new saved instance of {@link Component}
     */
    Component createComponent(String category, ModelNode modelNode);

    /**
     * Delete component from the database.
     * <p/>
     *
     * @param component a component to delete
     */
    void deleteComponent(Component component);

    /**
     * Retrieve a list of all components from the database.
     * <p/>
     *
     * @return a list with all saved components
     */
    List<Component> findComponents();

    /**
     * Retrieve a list of components for the specified category.
     * <p/>
     *
     * @param category a category to which a components related to
     * @return a list of components
     */
    List<Component> findComponents(String category);
}
