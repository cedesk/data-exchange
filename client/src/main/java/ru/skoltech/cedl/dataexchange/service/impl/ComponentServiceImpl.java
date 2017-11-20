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

import org.springframework.beans.factory.annotation.Autowired;
import ru.skoltech.cedl.dataexchange.entity.Component;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.repository.revision.ComponentRepository;
import ru.skoltech.cedl.dataexchange.service.ComponentService;
import ru.skoltech.cedl.dataexchange.service.ModelNodeService;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link ComponentService}.
 * <p/>
 * Created by Nikolay Groshkov on 17-Nov-17.
 */
public class ComponentServiceImpl implements ComponentService {

    private final ComponentRepository componentRepository;
    private ModelNodeService modelNodeService;

    @Autowired
    public ComponentServiceImpl(ComponentRepository componentRepository) {
        this.componentRepository = componentRepository;
    }

    public void setModelNodeService(ModelNodeService modelNodeService) {
        this.modelNodeService = modelNodeService;
    }

    @Override
    public Component createComponent(String category, ModelNode modelNode) {
        Objects.requireNonNull(category);
        Objects.requireNonNull(modelNode);

        ModelNode clonedModelNode = modelNodeService.cloneModelNode(modelNode.getName(), modelNode);
        clonedModelNode = modelNodeService.saveModelNode(clonedModelNode);
        Component component = new Component(clonedModelNode);
        component.setCategory(category);
        return componentRepository.saveAndFlush(component);
    }

    @Override
    public void deleteComponent(Component component) {
        Objects.requireNonNull(component);

        ModelNode modelNode = component.getModelNode();
        componentRepository.delete(component.getId());
        modelNodeService.deleteModelNode(modelNode);
    }

    @Override
    public List<Component> findComponents() {
        return componentRepository.findAll();
    }

    @Override
    public List<Component> findComponents(String category) {
        Objects.requireNonNull(category);

        return componentRepository.findAllByCategory(category);
    }
}
