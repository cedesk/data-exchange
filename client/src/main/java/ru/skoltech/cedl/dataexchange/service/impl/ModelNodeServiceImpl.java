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
    public ModelNode cloneModelNode(String name, ModelNode modelNode) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(modelNode);

        ModelNode newModelNode = this.createModelNode(name, modelNode.getClass());
        return this.deepModelNodeClone(newModelNode, modelNode);
    }

    @Override
    public ModelNode cloneModelNode(CompositeModelNode parentNode, String name, ModelNode modelNode) {
        Objects.requireNonNull(parentNode);
        Objects.requireNonNull(name);
        Objects.requireNonNull(modelNode);

        if (modelNode instanceof SystemModel) {
            throw new IllegalArgumentException("Cannot clone SystemModel. " +
                    "Either CompositeModelNode or ElementModel can be cloned.");
        }

        ModelNode newModelNode = this.createModelNode(parentNode, name);
        return this.deepModelNodeClone(newModelNode, modelNode);
    }

    @Override
    public ModelNode createModelNode(CompositeModelNode parentNode, String name) {
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
    public ModelNode createModelNode(String name, Class<? extends ModelNode> clazz) {
        Objects.requireNonNull(name);

        if (clazz == SystemModel.class) {
            return new SystemModel(name);
        } else if (clazz == SubSystemModel.class) {
            return new SubSystemModel(name);
        } else if (clazz == ElementModel.class) {
            return new ElementModel(name);
        } else if (clazz == InstrumentModel.class) {
            return new InstrumentModel(name);
        } else {
            throw new AssertionError("Must never be thrown.");
        }
    }

    @Override
    public void deleteModelNode(CompositeModelNode parentNode, ModelNode deleteNode, UserRoleManagement userRoleManagement) {
        Objects.requireNonNull(parentNode);
        Objects.requireNonNull(deleteNode);

        parentNode.removeSubNode(deleteNode);
        if (userRoleManagement == null) {
            return;
        }
        userRoleManagement.getDisciplineSubSystems()
                .removeIf(disciplineSubSystem -> disciplineSubSystem.getSubSystem() == deleteNode);
    }

    private ModelNode deepModelNodeClone(ModelNode newModelNode, ModelNode originalModelNode) {
        Objects.requireNonNull(newModelNode);
        Objects.requireNonNull(originalModelNode);
        assert newModelNode.getClass() == originalModelNode.getClass();

        newModelNode.setPosition(originalModelNode.getPosition());
        newModelNode.setDescription(originalModelNode.getDescription());
        newModelNode.setEmbodiment(originalModelNode.getEmbodiment());
        newModelNode.setCompletion(originalModelNode.isCompletion());

        if (originalModelNode instanceof CompositeModelNode) {
            CompositeModelNode newCompositeModelNode = (CompositeModelNode) newModelNode;
            if (originalModelNode instanceof SubSystemModel) {
                SubSystemModel subSystemModel = (SubSystemModel) originalModelNode;
                subSystemModel.getSubNodes()
                        .forEach(elementModel -> this.cloneModelNode(newCompositeModelNode, elementModel.getName(), elementModel));
            } else if (originalModelNode instanceof ElementModel) {
                ElementModel elementModel = (ElementModel) originalModelNode;
                elementModel.getSubNodes()
                        .forEach(instrumentModel -> this.cloneModelNode(newCompositeModelNode, instrumentModel.getName(), instrumentModel));
            }
        }

        // clone ExternalModels
        originalModelNode.getExternalModels()
                .forEach(externalModel -> {
                    ExternalModel newExternalModel = externalModelService.cloneExternalModel(externalModel, newModelNode);
                    newModelNode.addExternalModel(newExternalModel);
                });

        // clone ParameterModels
        originalModelNode.getParameters()
                .forEach(parameterModel -> {
                    ParameterModel newParameterModel = parameterModelService.cloneParameterModel(parameterModel.getName(), parameterModel, newModelNode);
                    newModelNode.addParameter(newParameterModel);
                });

        return newModelNode;
    }
}
