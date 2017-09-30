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
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.diff.AttributeDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ParameterDifference;

import java.util.List;

/**
 * Created by Nikolay Groshkov on 23-Aug-17.
 */
public interface ParameterDifferenceService {

    ParameterDifference createParameterAttributesModified(ParameterModel parameter1, ParameterModel parameter2, List<AttributeDifference> differences);

    ParameterDifference createRemovedParameter(ModelNode parent, ParameterModel param, String name, ModelDifference.ChangeLocation changeLocation);

    ParameterDifference createAddedParameter(ModelNode parent, ParameterModel param, String name, ModelDifference.ChangeLocation changeLocation);

    List<AttributeDifference> parameterDifferences(ParameterModel localParameterModel, ParameterModel remoteParameterModel);

    List<ParameterDifference> computeParameterDifferences(ModelNode localNode, ModelNode remoteNode, int currentRevisionNumber);

}
