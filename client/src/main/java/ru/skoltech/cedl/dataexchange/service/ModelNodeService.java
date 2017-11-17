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

import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.user.DisciplineSubSystem;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;

/**
 * Operations with model nodes.
 * <p/>
 * Created by Nikolay Groshkov on 10-Nov-17.
 */
public interface ModelNodeService {

    /**
     * Create an exact clone of the {@link ModelNode} instance along with all sub {@link ModelNode}s,
     * {@link ExternalModel}s and {@link ParameterModel}s.
     * <p/>
     * @param name       name of new {@link ModelNode} instance
     * @param modelNode  model node to clone from
     * @return a new instance of copied {@link ModelNode}
     */
    ModelNode cloneModelNode(String name, ModelNode modelNode);

    /**
     * Create an exact clone of the {@link ModelNode} instance along with all sub {@link ModelNode}s,
     * {@link ExternalModel}s and {@link ParameterModel}s for the specified parent {@link CompositeModelNode}.
     * <p/>
     *
     * @param parentNode parent {@link CompositeModelNode} for copied {@link ModelNode}
     * @param name       name of new {@link ModelNode} instance
     * @param modelNode  model node to clone from
     * @return a new instance of copied {@link ModelNode}
     */
    ModelNode cloneModelNode(CompositeModelNode parentNode, String name, ModelNode modelNode);

    /**
     * Create a new {@link ModelNode} and add to the passed parent with specified name.
     * <p/>
     *
     * @param parentNode parent node to add a new model node
     * @param name       a name of new {@link ModelNode}
     * @return an instance of added {@link ModelNode}
     */
    ModelNode createModelNode(CompositeModelNode parentNode, String name);

    /**
     * Create a new instance of extension of {@link ModelNode} specified by class name parameter .
     * <p/>
     *
     * @param name  a name of new {@link ModelNode}
     * @param clazz a type of the new instance
     * @return an instance of added {@link ModelNode}
     */
    ModelNode createModelNode(String name, Class<? extends ModelNode> clazz);

    /**
     * Delete a {@link ModelNode} from the passed parent. If {@link UserRoleManagement} argument is passed
     * than all {@link DisciplineSubSystem} with this deleting {@link ModelNode}
     * will be also removed from {@link UserRoleManagement}.
     * <p/>
     *
     * @param parentNode         parent node to delete a sub node
     * @param deleteNode         {@link ModelNode} to delete
     * @param userRoleManagement {@link UserRoleManagement} to delete all {@link DisciplineSubSystem}
     *                           of removed {@link ModelNode} in case of their existence
     */
    void deleteModelNode(CompositeModelNode parentNode, ModelNode deleteNode, UserRoleManagement userRoleManagement);

}
