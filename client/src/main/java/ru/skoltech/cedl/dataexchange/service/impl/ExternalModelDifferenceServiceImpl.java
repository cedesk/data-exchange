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

package ru.skoltech.cedl.dataexchange.service.impl;

import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.service.ExternalModelDifferenceService;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ExternalModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Nikolay Groshkov on 23-Aug-17.
 */
public class ExternalModelDifferenceServiceImpl implements ExternalModelDifferenceService {

    @Override
    public ExternalModelDifference createRemoveExternalModel(ModelNode parent, ExternalModel externalModel1, String name, ModelDifference.ChangeLocation changeLocation) {
        return new ExternalModelDifference(parent, externalModel1, name, ModelDifference.ChangeType.REMOVE, changeLocation);
    }

    @Override
    public ExternalModelDifference createAddExternalModel(ModelNode parent, ExternalModel externalModel1, String name, ModelDifference.ChangeLocation changeLocation) {
        return new ExternalModelDifference(parent, externalModel1, name, ModelDifference.ChangeType.ADD, changeLocation);
    }

    @Override
    public ExternalModelDifference createExternalModelModified(ExternalModel externalModel1, ExternalModel externalModel2, String name) {
        boolean e2newer = externalModel2.getLastModification() > externalModel1.getLastModification();
        ModelDifference.ChangeLocation changeLocation = e2newer ? ModelDifference.ChangeLocation.ARG2 : ModelDifference.ChangeLocation.ARG1;
        return new ExternalModelDifference(externalModel1, externalModel2, name, ModelDifference.ChangeType.MODIFY, changeLocation, "", "");
    }

    @Override
    public ExternalModelDifference createExternalModelModified(ExternalModel externalModel1, ExternalModel externalModel2, String name, String value1, String value2) {
        boolean n2newer = externalModel2.isNewerThan(externalModel1);
        ModelDifference.ChangeLocation changeLocation = n2newer ? ModelDifference.ChangeLocation.ARG2 : ModelDifference.ChangeLocation.ARG1;
        return new ExternalModelDifference(externalModel1, externalModel2, name, ModelDifference.ChangeType.MODIFY, changeLocation, value1, value2);
    }

    @Override
    public List<ModelDifference> computeExternalModelDifferences(ModelNode m1, ModelNode m2, Long latestStudy1Modification) {
        LinkedList<ModelDifference> extModelDifferences = new LinkedList<>();
        Map<String, ExternalModel> m1extModels = m1.getExternalModels().stream().collect(
                Collectors.toMap(ExternalModel::getUuid, Function.identity())
        );
        Map<String, ExternalModel> m2extModels = m2.getExternalModels().stream().collect(
                Collectors.toMap(ExternalModel::getUuid, Function.identity())
        );
        Set<String> allExtMods = new HashSet<>();
        allExtMods.addAll(m1extModels.keySet());
        allExtMods.addAll(m2extModels.keySet());

        for (String extMod : allExtMods) {
            ExternalModel e1 = m1extModels.get(extMod);
            ExternalModel e2 = m2extModels.get(extMod);

            if (e1 != null && e2 == null) {
                //if (e1.getLastModification() == null) { // model 1 was newly added
                extModelDifferences.add(createAddExternalModel(m1, e1, e1.getName(), ModelDifference.ChangeLocation.ARG1));
                //} else { // model 2 was deleted
                //    extModelDifferences.add(createRemoveExternalModel(m1, e1, e1.name(), ChangeLocation.ARG2));
                //}
            } else if (e1 == null && e2 != null) {
                Objects.requireNonNull(e2.getLastModification(), "persisted parameters always should have the timestamp set");
                if (e2.getLastModification() > latestStudy1Modification) { // model 2 was added
                    extModelDifferences.add(createAddExternalModel(m1, e2, e2.getName(), ModelDifference.ChangeLocation.ARG2));
                } else { // model 1 was deleted
                    extModelDifferences.add(createRemoveExternalModel(m1, e2, e2.getName(), ModelDifference.ChangeLocation.ARG1));
                }
            } else if (e1 != null && e2 != null) {
                if (!e1.getName().equals(e2.getName())) {
                    String value1 = e1.getName();
                    String value2 = e2.getName();
                    extModelDifferences.add(createExternalModelModified(e1, e2, "name", value1, value2));
                }
                if (!Arrays.equals(e1.getAttachment(), e2.getAttachment())) {
                    extModelDifferences.add(createExternalModelModified(e1, e2, "attachment"));
                }
            }
        }
        return extModelDifferences;
    }
}
