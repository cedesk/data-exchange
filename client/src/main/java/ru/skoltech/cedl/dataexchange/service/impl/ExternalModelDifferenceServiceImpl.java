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
        boolean e2newer = externalModel2.getRevision() > externalModel1.getRevision();
        ModelDifference.ChangeLocation changeLocation = e2newer ? ModelDifference.ChangeLocation.ARG2 : ModelDifference.ChangeLocation.ARG1;
        return new ExternalModelDifference(externalModel1, externalModel2, name, ModelDifference.ChangeType.MODIFY, changeLocation, "", "");
    }

    @Override
    public ExternalModelDifference createExternalModelModified(ExternalModel externalModel1, ExternalModel externalModel2, String name, String value1, String value2) {
        boolean n2newer = externalModel2.getRevision() > externalModel1.getRevision();
        ModelDifference.ChangeLocation changeLocation = n2newer ? ModelDifference.ChangeLocation.ARG2 : ModelDifference.ChangeLocation.ARG1;
        return new ExternalModelDifference(externalModel1, externalModel2, name, ModelDifference.ChangeType.MODIFY, changeLocation, value1, value2);
    }

    @Override
    public List<ModelDifference> computeExternalModelDifferences(ModelNode localNode, ModelNode remoteNode) {
        LinkedList<ModelDifference> extModelDifferences = new LinkedList<>();
        Map<String, ExternalModel> localExternalModels = localNode.getExternalModels().stream().collect(
                Collectors.toMap(ExternalModel::getUuid, Function.identity())
        );
        Map<String, ExternalModel> remoteExternalModels = remoteNode.getExternalModels().stream().collect(
                Collectors.toMap(ExternalModel::getUuid, Function.identity())
        );
        Set<String> allExtMods = new HashSet<>();
        allExtMods.addAll(localExternalModels.keySet());
        allExtMods.addAll(remoteExternalModels.keySet());

        for (String extMod : allExtMods) {
            ExternalModel localExternalModel = localExternalModels.get(extMod);
            ExternalModel remoteExternalModel = remoteExternalModels.get(extMod);

            if (localExternalModel != null && remoteExternalModel == null) {
                if (localExternalModel.getId() == 0) { // model 1 was newly added
                    extModelDifferences.add(createAddExternalModel(localNode, localExternalModel, localExternalModel.getName(), ModelDifference.ChangeLocation.ARG1));
                } else { // model 2 was deleted
                    extModelDifferences.add(createRemoveExternalModel(localNode, localExternalModel, localExternalModel.getName(), ModelDifference.ChangeLocation.ARG2));
                }
            } else if (localExternalModel == null && remoteExternalModel != null) {
                assert remoteExternalModel.getRevision() != 0; //persisted external models always should have the revision set
                if (remoteNode.getRevision() > localNode.getRevision()) { // node 2 was added
                    extModelDifferences.add(createAddExternalModel(localNode, remoteExternalModel, remoteExternalModel.getName(), ModelDifference.ChangeLocation.ARG2));
                } else { // model 1 was deleted
                    extModelDifferences.add(createRemoveExternalModel(localNode, remoteExternalModel, remoteExternalModel.getName(), ModelDifference.ChangeLocation.ARG1));
                }
            } else if (localExternalModel != null) {
                if (!localExternalModel.getName().equals(remoteExternalModel.getName())) {
                    String value1 = localExternalModel.getName();
                    String value2 = remoteExternalModel.getName();
                    extModelDifferences.add(createExternalModelModified(localExternalModel, remoteExternalModel, "name", value1, value2));
                }
                if (!Arrays.equals(localExternalModel.getAttachment(), remoteExternalModel.getAttachment())) {
                    extModelDifferences.add(createExternalModelModified(localExternalModel, remoteExternalModel, "attachment"));
                }
            }
        }
        return extModelDifferences;
    }
}
