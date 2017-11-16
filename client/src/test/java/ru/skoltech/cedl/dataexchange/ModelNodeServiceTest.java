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
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.model.*;
import ru.skoltech.cedl.dataexchange.entity.user.Discipline;
import ru.skoltech.cedl.dataexchange.entity.user.UserManagement;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.service.ModelNodeService;
import ru.skoltech.cedl.dataexchange.service.UserManagementService;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.structure.BasicSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilder;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Nikolay Groshkov on 10-Nov-17.
 */
public class ModelNodeServiceTest extends AbstractApplicationContextTest {

    private SystemBuilder systemBuilder;
    private ModelNodeService modelNodeService;
    private UserManagementService userManagementService;
    private UserRoleManagementService userRoleManagementService;

    private SystemModel systemModel;
    private SubSystemModel subSystemModel;

    @Before
    public void prepare() {
        modelNodeService = context.getBean(ModelNodeService.class);
        userManagementService = context.getBean(UserManagementService.class);
        userRoleManagementService = context.getBean(UserRoleManagementService.class);
        systemBuilder = context.getBean(BasicSpaceSystemBuilder.class);
        systemBuilder.modelDepth(4);


        systemModel = new SystemModel("systemModel");
        subSystemModel = new SubSystemModel("subSystemModel");

        systemModel.addSubNode(subSystemModel);
        subSystemModel.setParent(systemModel);
        assertEquals(1, systemModel.getSubNodes().size());
        assertThat(systemModel.getSubNodes(), contains(subSystemModel));
    }

    @Test
    public void testAddSubNode() {
        SystemModel systemModel = new SystemModel("systemModel");
        String subSystemModelName = "subSystemModelName";
        assertTrue(systemModel.getSubNodes().isEmpty());
        ModelNode addedModelNode1 = modelNodeService.addSubNode(systemModel, subSystemModelName);
        assertNotNull(addedModelNode1);
        assertThat(addedModelNode1, instanceOf(SubSystemModel.class));
        assertEquals(subSystemModelName, addedModelNode1.getName());
        assertEquals(systemModel, addedModelNode1.getParent());
        assertEquals(1, systemModel.getSubNodes().size());
        assertEquals(addedModelNode1, systemModel.getSubNodes().get(0));

        SubSystemModel subSystemModel = new SubSystemModel("subSystemModel");
        String elementModelName = "elementModelName";
        assertTrue(subSystemModel.getSubNodes().isEmpty());
        ModelNode addedModelNode2 = modelNodeService.addSubNode(subSystemModel, elementModelName);
        assertNotNull(addedModelNode2);
        assertThat(addedModelNode2, instanceOf(ElementModel.class));
        assertEquals(elementModelName, addedModelNode2.getName());
        assertEquals(subSystemModel, addedModelNode2.getParent());
        assertEquals(1, subSystemModel.getSubNodes().size());
        assertEquals(addedModelNode2, subSystemModel.getSubNodes().get(0));

        ElementModel elementModel = new ElementModel("elementModel");
        String instrumentModelName = "instrumentModelName";
        assertTrue(elementModel.getSubNodes().isEmpty());
        ModelNode addedModelNode3 = modelNodeService.addSubNode(elementModel, instrumentModelName);
        assertNotNull(addedModelNode3);
        assertThat(addedModelNode3, instanceOf(InstrumentModel.class));
        assertEquals(instrumentModelName, addedModelNode3.getName());
        assertEquals(elementModel, addedModelNode3.getParent());
        assertEquals(1, elementModel.getSubNodes().size());
        assertEquals(addedModelNode3, elementModel.getSubNodes().get(0));
    }

    @Test(expected = NullPointerException.class)
    public void testAddSubNodeFail1() {
        modelNodeService.addSubNode(null, "name");
    }

    @Test(expected = NullPointerException.class)
    public void testAddSubNodeFail2() {
        modelNodeService.addSubNode(systemModel, null);
    }

    @Test
    public void testCloneSubNode() {
        SystemModel systemModel = systemBuilder.build("systemName");
        ExternalModel externalModel = mock(ExternalModel.class);
        when(externalModel.getName()).thenReturn("test.xls");

        assertFalse(systemModel.getSubNodes().isEmpty());
        SubSystemModel originalModelNode = systemModel.getSubNodes().get(0);
        originalModelNode.addExternalModel(externalModel);
        String name = "name";
        ModelNode clonedModelNode = modelNodeService.cloneSubNode(systemModel, name, originalModelNode);

        assertNotNull(clonedModelNode);
        assertTrue(clonedModelNode instanceof SubSystemModel);
        assertEquals(name, clonedModelNode.getName());
        assertEquals(systemModel, clonedModelNode.getParent());
        assertFalse(originalModelNode == clonedModelNode);
        assertEquals(originalModelNode.getExternalModels().size(), clonedModelNode.getExternalModelMap().size());
        assertEquals(originalModelNode.getParameters().size(), clonedModelNode.getParameters().size());

        Map<String, ModelNode> originalSubNodesMap = originalModelNode.getSubNodesMap();
        Map<String, ModelNode> clonedSubNodesMap = ((SubSystemModel) clonedModelNode).getSubNodesMap();
        clonedSubNodesMap.forEach((key, value) -> {
            assertTrue(originalSubNodesMap.containsKey(key));
            assertFalse(originalSubNodesMap.get(key) == value);
        });

        Map<String, ExternalModel> originalExternalModelMap = originalModelNode.getExternalModelMap();
        Map<String, ExternalModel> clonedExternalModelMap = clonedModelNode.getExternalModelMap();
        clonedExternalModelMap.forEach((key, value) -> {
            assertTrue(originalExternalModelMap.containsKey(key));
            assertFalse(originalExternalModelMap.get(key) == value);
        });

        Map<String, ParameterModel> originalParameterModelMap = originalModelNode.getParameterMap();
        Map<String, ParameterModel> clonedParameterModelMap = clonedModelNode.getParameterMap();
        clonedParameterModelMap.forEach((key, value) -> {
            assertTrue(originalParameterModelMap.containsKey(key));
            assertFalse(originalParameterModelMap.get(key) == value);
        });
    }

    @Test(expected = NullPointerException.class)
    public void testCloneSubNodeFail1() {
        modelNodeService.cloneSubNode(null, "name", subSystemModel);
    }

    @Test(expected = NullPointerException.class)
    public void testCloneSubNodeFail2() {
        modelNodeService.cloneSubNode(systemModel, null, subSystemModel);
    }

    @Test(expected = NullPointerException.class)
    public void testCloneSubNodeFail3() {
        modelNodeService.cloneSubNode(systemModel, "name", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCloneSubNodeFail4() {
        modelNodeService.cloneSubNode(systemModel, "name", systemModel);
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteNodeFail1() {
        modelNodeService.deleteNode(null, subSystemModel, null);
    }

    @Test(expected = NullPointerException.class)
    public void testDeleteNodeFail2() {
        modelNodeService.deleteNode(systemModel, null, null);
    }

    @Test
    public void testDeleteNodeWithUserRoleManager() {
        UserManagement userManagement = userManagementService.createDefaultUserManagement();
        UserRoleManagement userRoleManagement = userRoleManagementService.createUserRoleManagementWithSubsystemDisciplines(systemModel, userManagement);

        userRoleManagement.getDisciplines().forEach(discipline -> {
            boolean added = userRoleManagementService.addDisciplineSubsystem(userRoleManagement, discipline, subSystemModel);
            assertFalse(added);
        });

        Discipline discipline = userRoleManagement.getDisciplines().stream()
                .filter(d -> d.getName().equals(subSystemModel.getName())).findFirst().orElse(null);

        assertNotNull(discipline);
        assertEquals(2, userRoleManagement.getDisciplineSubSystems().size());
        assertThat(userRoleManagement.getDisciplineSubSystems(),
                hasItem(
                        allOf(hasProperty("discipline", is(discipline)),
                                hasProperty("subSystem", is(subSystemModel)))));

        modelNodeService.deleteNode(systemModel, subSystemModel, userRoleManagement);
        assertTrue(systemModel.getSubNodes().isEmpty());

        assertThat(userRoleManagement.getDisciplineSubSystems(),
                not(hasItem(
                        allOf(hasProperty("discipline", is(discipline)),
                                hasProperty("subSystem", is(subSystemModel))))));

    }

    @Test
    public void testDeleteNodeWithoutUserRoleManager() {
        modelNodeService.deleteNode(systemModel, subSystemModel, null);
        assertTrue(systemModel.getSubNodes().isEmpty());
    }

}
