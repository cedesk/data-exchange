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
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.model.*;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.service.ExternalModelService;
import ru.skoltech.cedl.dataexchange.service.ModelNodeService;
import ru.skoltech.cedl.dataexchange.service.ParameterModelService;

import java.util.Objects;

/**
 * Implementation of {@link ModelNodeService}.
 * <p/>
 * Created by D.Knoll on 29.03.2015.
 */
public class ModelNodeServiceImpl implements ModelNodeService {

    private ExternalModelService externalModelService;
    private ParameterModelService parameterModelService;

    public void setExternalModelService(ExternalModelService externalModelService) {
        this.externalModelService = externalModelService;
    }

    public void setParameterModelService(ParameterModelService parameterModelService) {
        this.parameterModelService = parameterModelService;
    }

    @Override
    public ModelNode addSubNode(CompositeModelNode parentNode, String name) {
        Objects.requireNonNull(parentNode);
        Objects.requireNonNull(name);

        if (parentNode instanceof SystemModel) {
            SystemModel systemModel = (SystemModel) parentNode;
            SubSystemModel subSystemModel = new SubSystemModel(name);
            systemModel.addSubNode(subSystemModel);
            return subSystemModel;
        } else if (parentNode instanceof SubSystemModel) {
            SubSystemModel subSystemModel = (SubSystemModel) parentNode;
            ElementModel elementModel = new ElementModel(name);
            subSystemModel.addSubNode(elementModel);
            return elementModel;
        } else if (parentNode instanceof ElementModel) {
            ElementModel elementModel = (ElementModel) parentNode;
            InstrumentModel instrumentModel = new InstrumentModel(name);
            elementModel.addSubNode(instrumentModel);
            return instrumentModel;
        } else {
            throw new AssertionError("Unexpected type of parent node: " + parentNode.getClass().getName() + "." +
                    "Must never be thrown.");
        }
    }

    @Override
    public ModelNode cloneSubNode(CompositeModelNode parentNode, String name, ModelNode modelNode) {
        Objects.requireNonNull(parentNode);
        Objects.requireNonNull(name);
        Objects.requireNonNull(modelNode);

        if (modelNode instanceof SystemModel) {
            throw new IllegalArgumentException("Cannot clone SystemModel. " +
                    "Either CompositeModelNode or ElementModel can be cloned.");
        }

        // clone ModelNode
        ModelNode newModelNode = this.addSubNode(parentNode, name);
        assert newModelNode.getClass() == modelNode.getClass();

        if (modelNode instanceof CompositeModelNode) {
            CompositeModelNode newCompositeModelNode = (CompositeModelNode) newModelNode;
            if (modelNode instanceof SubSystemModel) {
                SubSystemModel subSystemModel = (SubSystemModel) modelNode;
                subSystemModel.getSubNodes()
                        .forEach(elementModel -> this.cloneSubNode(newCompositeModelNode, elementModel.getName(), elementModel));
            } else if (modelNode instanceof ElementModel) {
                ElementModel elementModel = (ElementModel) modelNode;
                elementModel.getSubNodes()
                        .forEach(instrumentModel -> this.cloneSubNode(newCompositeModelNode, instrumentModel.getName(), instrumentModel));
            }
        }

        // clone ExternalModels
        modelNode.getExternalModels()
                .forEach(externalModel -> {
                    ExternalModel newExternalModel = externalModelService.cloneExternalModel(externalModel, newModelNode);
                    newModelNode.addExternalModel(newExternalModel);
                });

        // clone ParameterModels
        modelNode.getParameters()
                .forEach(parameterModel -> {
                    ParameterModel newParameterModel = parameterModelService.cloneParameterModel(parameterModel.getName(), parameterModel, modelNode);
                    newModelNode.addParameter(newParameterModel);
                });

        return newModelNode;
    }

    @Override
    public void deleteNode(CompositeModelNode parentNode, ModelNode deleteNode, UserRoleManagement userRoleManagement) {
        Objects.requireNonNull(parentNode);
        Objects.requireNonNull(deleteNode);

        parentNode.removeSubNode(deleteNode);
        if (userRoleManagement == null) {
            return;
        }
        userRoleManagement.getDisciplineSubSystems()
                .removeIf(disciplineSubSystem -> disciplineSubSystem.getSubSystem() == deleteNode);
    }
}
