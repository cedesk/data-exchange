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

import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterRevision;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;

import java.util.List;

/**
 * Operations with {@link ParameterModel}.
 * <p>
 * Created by Nikolay Groshkov on 07-Aug-17.
 */
public interface ParameterModelService {

    /**
     * Create a exact clone of {@link ParameterModel} instance with specified name for the same {@link ModelNode}
     * as the parent of the specified {@link ParameterModel} to clone from.
     * <p/>
     * This method registers a parent for new instance of {@link ParameterModel} but it is still required
     * to add this instance to the list of supported {@link ParameterModel}s of this parent {@link ModelNode}.
     * <p/>
     *
     * @param name           name of new {@link ParameterModel} instance
     * @param parameterModel instance of {@link ParameterModel} to clone from
     * @return a new instance of copied {@link ParameterModel}
     */
    ParameterModel cloneParameterModel(String name, ParameterModel parameterModel);

    /**
     * Create a exact clone of {@link ParameterModel} instance for the specified {@link ModelNode}.
     * <p/>
     * This method registers a parent for new instance of {@link ParameterModel} but it is still required
     * to add this instance to the list of supported {@link ParameterModel}s of this parent {@link ModelNode}.
     * <p/>
     *
     * @param name            name of new {@link ParameterModel} instance
     * @param parameterModel  instance of {@link ParameterModel} to clone from
     * @param parentModelNode parent {@link ModelNode} for copied {@link ParameterModel}
     * @return a new instance of copied {@link ParameterModel}
     */
    ParameterModel cloneParameterModel(String name, ParameterModel parameterModel, ModelNode parentModelNode);

    /**
     * Retrieve a list of parameter revisions for specified {@link ParameterModel}.
     * <p/>
     *
     * @param parameterModel parameter model
     * @return list of parameter revisions
     */
    List<ParameterRevision> parameterModelChangeHistory(ParameterModel parameterModel);

}
