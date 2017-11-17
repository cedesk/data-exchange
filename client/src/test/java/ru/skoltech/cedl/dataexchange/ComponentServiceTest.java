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

package ru.skoltech.cedl.dataexchange;

import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.Component;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.service.ComponentService;
import ru.skoltech.cedl.dataexchange.service.ModelNodeService;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Nikolay Groshkov on 17-Nov-17.
 */
public class ComponentServiceTest extends AbstractApplicationContextTest {

    private ComponentService componentService;
    private SubSystemModel modelNode;

    @Before
    public void prepare() {
        componentService = context.getBean(ComponentService.class);
        ModelNodeService modelNodeService = context.getBean(ModelNodeService.class);
        SubSystemModel modelNode = modelNodeService.createModelNode("name", SubSystemModel.class);
        this.modelNode = modelNodeService.saveModelNode(modelNode);

    }

    @Test
    public void testCreateComponent() {
        String category = "category";
        Component component = componentService.createComponent(category, modelNode);
        assertNotNull(component);
        assertNotEquals(0, component.getId());
        assertNotEquals(modelNode, component.getModelNode());
        assertEquals(category, component.getCategory());
        componentService.deleteComponent(component);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateComponentFail1() {
        componentService.createComponent(null, modelNode);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateComponentFail2() {
        componentService.createComponent("category", null);
    }

    @Test
    public void testDeleteComponent() {
        Component component = componentService.createComponent("category", modelNode);
        List<Component> componentsList = componentService.findComponents();
        assertNotNull(componentsList);
        assertEquals(1, componentsList.size());

        componentService.deleteComponent(component);

        List<Component> emptyList = componentService.findComponents();
        assertNotNull(emptyList);
        assertTrue(emptyList.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteComponentFail() {
        componentService.deleteComponent(null);
    }

    @Test
    public void testFindComponents() {
        List<Component> emptyList = componentService.findComponents("emptyCategory");
        assertNotNull(emptyList);
        assertTrue(emptyList.isEmpty());

        String category = "category";
        componentService.createComponent(category, modelNode);
        List<Component> componentsList1 = componentService.findComponents();
        assertNotNull(componentsList1);
        assertEquals(1, componentsList1.size());

        List<Component> componentsList2 = componentService.findComponents(category);
        assertNotNull(componentsList2);
        assertEquals(1, componentsList2.size());
    }

    @Test(expected = NullPointerException.class)
    public void testFindComponentsFail() {
        componentService.findComponents(null);
    }
}
