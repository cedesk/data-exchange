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

package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.revision.SystemModelRepository;
import ru.skoltech.cedl.dataexchange.service.NodeDifferenceService;
import ru.skoltech.cedl.dataexchange.structure.model.diff.MergeException;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;

import java.util.List;

import static org.junit.Assert.*;
import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeLocation;
import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeType;

/**
 * Created by D.Knoll on 25.05.2017.
 */
public class NodeDifferenceServiceTest extends AbstractApplicationContextTest {

    private NodeDifferenceService nodeDifferenceService;
    private SystemModelRepository systemModelRepository;

    private SystemModel baseSystemModel;

    @Before
    public void prepare() {
        nodeDifferenceService = context.getBean(NodeDifferenceService.class);
        systemModelRepository = context.getBean(SystemModelRepository.class);

        baseSystemModel = new SystemModel();
        baseSystemModel.setName("SM");
    }

    @Test
    public void localNodeAdd() throws Exception {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        int currentRevisionNumber = localSystem.getRevision();

        SubSystemModel newLocalSub = new SubSystemModel("subsys");
        localSystem.addSubNode(newLocalSub);

        assertEquals(1, localSystem.getSubNodes().size());

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        assertEquals(ChangeLocation.ARG1, md.getChangeLocation());
        assertEquals(ChangeType.ADD, md.getChangeType());
        assertEquals(newLocalSub.getNodePath(), md.getElementPath());

        assertTrue(md.isRevertible());

        md.revertDifference();

        assertEquals(0, localSystem.getSubNodes().size());

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        assertEquals(0, differences.size());
    }

    @Test
    public void localNodeModify() {
        SubSystemModel subSystem = new SubSystemModel("subSystem");

        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        localSystem.addSubNode(subSystem);
        localSystem = systemModelRepository.saveAndFlush(localSystem);

        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        int currentRevisionNumber = localSystem.getRevision();

        subSystem = localSystem.getSubNodes().get(0);
        subSystem.setName(subSystem.getName() + "-v2");
        subSystem.setDescription("description");
        subSystem.setEmbodiment("embodiment");
        subSystem.setCompletion(true);

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        assertEquals(4, differences.size());
        differences.forEach(modelDifference -> {
            assertEquals(ChangeLocation.ARG1, modelDifference.getChangeLocation());
            assertEquals(ChangeType.MODIFY, modelDifference.getChangeType());
            assertTrue(modelDifference.isRevertible());
            try {
                modelDifference.revertDifference();
            } catch (MergeException e) {
                fail();
            }
        });


        assertEquals(1, localSystem.getSubNodes().size());
        assertTrue(localSystem.getSubNodes().get(0).equals(remoteSystem.getSubNodes().get(0)));

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        assertEquals(0, differences.size());
    }

    @Test
    public void localNodeRemove() throws Exception {
        SubSystemModel subSystem = new SubSystemModel("subSystem");

        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        localSystem.addSubNode(subSystem);
        localSystem = systemModelRepository.saveAndFlush(localSystem);

        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        int currentRevisionNumber = localSystem.getRevision();

        localSystem.getSubNodes().clear();

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        assertEquals(ChangeLocation.ARG1, md.getChangeLocation());
        assertEquals(ChangeType.REMOVE, md.getChangeType());

        assertTrue(md.isRevertible());

        md.revertDifference();

        assertEquals(1, localSystem.getSubNodes().size());
        assertTrue(localSystem.getSubNodes().get(0).equals(remoteSystem.getSubNodes().get(0)));

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        assertEquals(0, differences.size());
    }

    @Test
    public void remoteNodeAdd() throws Exception {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        int currentRevisionNumber = localSystem.getRevision();

        SubSystemModel subSystem = new SubSystemModel("subsystem");
        remoteSystem.addSubNode(subSystem);
        remoteSystem = systemModelRepository.saveAndFlush(remoteSystem);

        int lastRevisionNumber = remoteSystem.getRevision();
        assertTrue(currentRevisionNumber < lastRevisionNumber);

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        assertEquals(ChangeLocation.ARG2, md.getChangeLocation());
        assertEquals(ChangeType.ADD, md.getChangeType());

        assertTrue(md.isMergeable());
        md.mergeDifference();

        assertEquals(1, localSystem.getSubNodes().size());
        assertTrue(localSystem.getSubNodes().get(0).equals(remoteSystem.getSubNodes().get(0)));

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        assertEquals(0, differences.size());
    }

    @Test
    public void remoteNodeModify() throws Exception {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);

        SubSystemModel subSystem = new SubSystemModel("subsystem");
        localSystem.addSubNode(subSystem);
        localSystem = systemModelRepository.saveAndFlush(localSystem);

        int currentRevisionNumber = localSystem.getRevision();

        SystemModel remoteSystem = systemModelRepository.saveAndFlush(localSystem);
        subSystem = remoteSystem.getSubNodes().get(0);
        subSystem.setName(subSystem.getName() + "_v3");
        remoteSystem = systemModelRepository.saveAndFlush(remoteSystem);

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        assertEquals(ChangeLocation.ARG2, md.getChangeLocation());
        assertEquals(ChangeType.MODIFY, md.getChangeType());

        assertTrue(md.isMergeable());
        md.mergeDifference();

        assertEquals(1, localSystem.getSubNodes().size());
        assertTrue(localSystem.getSubNodes().get(0).equals(remoteSystem.getSubNodes().get(0)));

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        assertEquals(0, differences.size());
    }

    @Test
    public void remoteNodeRemove() throws Exception {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);

        SubSystemModel subSystem = new SubSystemModel("subsystem");
        localSystem.addSubNode(subSystem);
        localSystem = systemModelRepository.saveAndFlush(localSystem);

        int currentRevisionNumber = localSystem.getRevision();

        SystemModel remoteSystem = systemModelRepository.saveAndFlush(localSystem);
        remoteSystem.getSubNodes().clear();

        remoteSystem = systemModelRepository.saveAndFlush(remoteSystem);

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        assertEquals(ChangeLocation.ARG2, md.getChangeLocation());
        assertEquals(ChangeType.REMOVE, md.getChangeType());

        assertTrue(md.isMergeable());
        md.mergeDifference();

        assertEquals(0, localSystem.getSubNodes().size());

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        assertEquals(0, differences.size());
    }

}
