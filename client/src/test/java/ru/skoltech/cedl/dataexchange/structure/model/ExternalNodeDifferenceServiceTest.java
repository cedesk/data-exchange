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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.revision.SystemModelRepository;
import ru.skoltech.cedl.dataexchange.service.ExternalModelFileStorageService;
import ru.skoltech.cedl.dataexchange.service.NodeDifferenceService;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeLocation;
import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeType;

/**
 * Created by D.Knoll on 25.05.2017.
 */
public class ExternalNodeDifferenceServiceTest extends AbstractApplicationContextTest {

    private NodeDifferenceService nodeDifferenceService;
    private SystemModelRepository systemModelRepository;

    private SystemModel baseSystemModel;

    private ExternalModel extMod;

    @Before
    public void prepare() throws IOException, URISyntaxException {
        nodeDifferenceService = context.getBean(NodeDifferenceService.class);
        systemModelRepository = context.getBean(SystemModelRepository.class);
        ExternalModelFileStorageService externalModelFileStorageService = context.getBean(ExternalModelFileStorageService.class);

        baseSystemModel = new SystemModel();
        baseSystemModel.setName("SM");

        File externalModelFile = new File(this.getClass().getResource("/attachment.xls").toURI());
        extMod = externalModelFileStorageService.createExternalModelFromFile(externalModelFile, baseSystemModel);
    }

    @Test
    public void localNodeAdd() throws Exception {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        int currentRevisionNumber = localSystem.getRevision();

        localSystem.addExternalModel(extMod);

        Assert.assertEquals(1, localSystem.getExternalModels().size());

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG1, md.getChangeLocation());
        Assert.assertEquals(ChangeType.ADD, md.getChangeType());
        Assert.assertEquals(md.getElementPath(), extMod.getNodePath());

        Assert.assertTrue(md.isRevertible());

        md.revertDifference();

        Assert.assertEquals(0, localSystem.getExternalModels().size());

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(0, differences.size());

    }

    @Test
    public void localNodeModify() throws Exception {
        ExternalModel extModel = extMod;

        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        localSystem.addExternalModel(extModel);
        localSystem = systemModelRepository.saveAndFlush(localSystem);

        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        int currentRevisionNumber = localSystem.getRevision();

        extModel = localSystem.getExternalModels().get(0);
        extModel.setName(extModel.getName() + "-v2");

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG1, md.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, md.getChangeType());

        Assert.assertTrue(md.isRevertible());

        md.revertDifference();

        Assert.assertEquals(1, localSystem.getExternalModels().size());
        Assert.assertTrue(localSystem.getExternalModels().get(0).equals(remoteSystem.getExternalModels().get(0)));
        Assert.assertTrue(localSystem.getExternalModels().get(0).getParent() == localSystem);

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void localNodeRemove() throws Exception {
        ExternalModel existingRemoteNode = extMod;

        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        localSystem.addExternalModel(existingRemoteNode);
        localSystem = systemModelRepository.saveAndFlush(localSystem);

        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        int currentRevisionNumber = localSystem.getRevision();

        localSystem.getExternalModels().clear();

        Assert.assertEquals(1, remoteSystem.getExternalModels().size());
        Assert.assertEquals(0, localSystem.getExternalModels().size());

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG1, md.getChangeLocation());
        Assert.assertEquals(ChangeType.REMOVE, md.getChangeType());

        Assert.assertTrue(md.isRevertible());

        md.revertDifference();

        Assert.assertEquals(1, localSystem.getExternalModels().size());
        Assert.assertTrue(localSystem.getExternalModels().get(0).equals(remoteSystem.getExternalModels().get(0)));

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteNodeAdd() throws Exception {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        int currentRevisionNumber = localSystem.getRevision();

        ExternalModel extModel = extMod;
        remoteSystem.addExternalModel(extModel);
        remoteSystem = systemModelRepository.saveAndFlush(remoteSystem);

        int lastRevisionNumber = remoteSystem.getRevision();
        Assert.assertTrue(currentRevisionNumber < lastRevisionNumber);

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG2, md.getChangeLocation());
        Assert.assertEquals(ChangeType.ADD, md.getChangeType());

        Assert.assertTrue(md.isMergeable());
        md.mergeDifference();

        Assert.assertEquals(1, localSystem.getExternalModels().size());
        Assert.assertTrue(localSystem.getExternalModels().get(0).equals(remoteSystem.getExternalModels().get(0)));

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteNodeModify() throws Exception {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);

        ExternalModel extModel = extMod;
        localSystem.addExternalModel(extModel);
        localSystem = systemModelRepository.saveAndFlush(localSystem);

        int currentRevisionNumber = localSystem.getRevision();

        SystemModel remoteSystem = systemModelRepository.saveAndFlush(localSystem);
        extModel = remoteSystem.getExternalModels().get(0);
        extModel.setName(extModel.getName() + "_v3");
        remoteSystem = systemModelRepository.saveAndFlush(remoteSystem);

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG2, md.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, md.getChangeType());

        Assert.assertTrue(md.isMergeable());
        md.mergeDifference();

        Assert.assertEquals(1, localSystem.getExternalModels().size());
        Assert.assertTrue(localSystem.getExternalModels().get(0).equals(remoteSystem.getExternalModels().get(0)));
        Assert.assertTrue(localSystem.getExternalModels().get(0).getParent() == localSystem);

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteNodeRemove() throws Exception {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);

        ExternalModel existingLocalNode = extMod;
        localSystem.addExternalModel(existingLocalNode);
        localSystem = systemModelRepository.saveAndFlush(localSystem);
        int currentRevisionNumber = localSystem.getRevision();

        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        remoteSystem.getExternalModels().clear();
        remoteSystem = systemModelRepository.saveAndFlush(remoteSystem);


        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG2, md.getChangeLocation());
        Assert.assertEquals(ChangeType.REMOVE, md.getChangeType());

        Assert.assertTrue(md.isMergeable());
        md.mergeDifference();

        Assert.assertEquals(0, localSystem.getExternalModels().size());

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(0, differences.size());
    }

}
