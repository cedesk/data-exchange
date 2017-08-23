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
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ExternalModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;

import java.util.List;

/**
 * Created by Nikolay Groshkov on 23-Aug-17.
 */
public interface ExternalModelDifferenceService {

        ExternalModelDifference createRemoveExternalModel(ModelNode parent, ExternalModel externalModel1, String name, ModelDifference.ChangeLocation changeLocation);
    ExternalModelDifference createAddExternalModel(ModelNode parent, ExternalModel externalModel1, String name, ModelDifference.ChangeLocation changeLocation);
    ExternalModelDifference createExternalModelModified(ExternalModel externalModel1, ExternalModel externalModel2, String name);
    ExternalModelDifference createExternalModelModified(ExternalModel externalModel1, ExternalModel externalModel2, String name, String value1, String value2);
    List<ModelDifference> computeExternalModelDifferences(ModelNode m1, ModelNode m2, Long latestStudy1Modification);

}
