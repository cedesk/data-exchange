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
import ru.skoltech.cedl.dataexchange.entity.ext.ExcelExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.revision.SystemModelRepository;
import ru.skoltech.cedl.dataexchange.service.NodeDifferenceService;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilderFactory;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;

import java.util.List;

import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeType;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public class ModelDifferencesTest extends AbstractApplicationContextTest {

    private NodeDifferenceService nodeDifferenceService;
    private SystemModelRepository systemModelRepository;
    private SystemBuilderFactory systemBuilderFactory;

    private SystemModel baseSystemModel;

    @Before
    public void prepare() {
        nodeDifferenceService = context.getBean(NodeDifferenceService.class);
        systemModelRepository = context.getBean(SystemModelRepository.class);
        systemBuilderFactory = context.getBean(SystemBuilderFactory.class);

        baseSystemModel = new SystemModel();
        baseSystemModel.setName("SM");
    }

    @Test
    public void equalNodes() {
        SystemBuilder systemBuilder = systemBuilderFactory.getBuilder("Basic Space System");
        systemBuilder.modelDepth(4);

        SystemModel localSystem = systemBuilder.build("testSystem");
        SystemModel localSystemBBBB = localSystem;
        localSystem = systemModelRepository.saveAndFlush(localSystem);
        int currentRevisionNumber = localSystem.getRevision();
        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());

        Assert.assertTrue(localSystem.getId() == remoteSystem.getId());
        List<ModelDifference> modelDifferences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(0, modelDifferences.size());
    }

    @Test
    public void twoNodeDiffer3() {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        SubSystemModel su1 = new SubSystemModel("subnode");
        localSystem.addSubNode(su1);
        localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        int currentRevisionNumber = localSystem.getRevision();

        ParameterModel p3 = new ParameterModel("new-param", 0.24);
        localSystem.addParameter(p3);
        ExternalModel externalModel3 = new ExcelExternalModel();
        externalModel3.setName("otherfile.tmp");
        localSystem.addExternalModel(externalModel3);

        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        remoteSystem.getSubNodes().clear();
        remoteSystem = systemModelRepository.saveAndFlush(remoteSystem);

        List<ModelDifference> modelDifferences =
                nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        System.out.println(modelDifferences);

        Assert.assertEquals(3, modelDifferences.size());
        Assert.assertEquals(ChangeType.ADD, modelDifferences.get(0).getChangeType());
        Assert.assertEquals(ChangeType.ADD, modelDifferences.get(1).getChangeType());
        Assert.assertEquals(ChangeType.REMOVE, modelDifferences.get(2).getChangeType());
    }

    @Test
    public void twoNodeDiffersOnlyInName() {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        int currentRevisionNumber = localSystem.getRevision();

        localSystem.setName("NewSM");

        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());

        List<ModelDifference> modelDifferences =
                nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);

        Assert.assertEquals(1, modelDifferences.size());
    }

    @Test
    public void twoNodeDiffersOnlyInNameAndOneParameter() {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        int currentRevisionNumber = localSystem.getRevision();

        localSystem.setName("NewSM");

        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        ParameterModel p3 = new ParameterModel("new-param", 0.24);
        p3.setRevision(100);
        remoteSystem.addParameter(p3);
        remoteSystem = systemModelRepository.saveAndFlush(remoteSystem);

        List<ModelDifference> modelDifferences =
                nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        System.out.println(modelDifferences);

        Assert.assertEquals(2, modelDifferences.size());
    }

    @Test
    public void twoNodeWithParams() {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        SubSystemModel u1 = new SubSystemModel("u1");
        ParameterModel p1 = new ParameterModel("new-param", 0.2);
        u1.addParameter(p1);
        localSystem.addSubNode(u1);
        localSystem = systemModelRepository.saveAndFlush(localSystem);
        int currentRevisionNumber = localSystem.getRevision();

        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());

        localSystem.setName("NewSM");
        u1 = localSystem.getSubNodes().get(0);
        p1 = u1.getParameters().get(0);
        p1.setValue(0.5);

        List<ModelDifference> modelDifferences =
                nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        System.out.println(modelDifferences);

        Assert.assertEquals(2, modelDifferences.size());
        Assert.assertEquals(ChangeType.MODIFY, modelDifferences.get(0).getChangeType());
        Assert.assertEquals(ChangeType.MODIFY, modelDifferences.get(1).getChangeType());
    }

    @Test
    public void twoNodeWithSubNodes() {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        SubSystemModel u1 = new SubSystemModel("u1");
        localSystem.addSubNode(u1);
        localSystem = systemModelRepository.saveAndFlush(localSystem);
        int currentRevisionNumber = localSystem.getRevision();

        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());

        localSystem.setName("NewSM");
        u1 = localSystem.getSubNodes().get(0);
        ParameterModel p1 = new ParameterModel("new-param", 0.24);
        u1.addParameter(p1);

        List<ModelDifference> modelDifferences =
                nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, currentRevisionNumber);
        System.out.println(modelDifferences);

        Assert.assertEquals(2, modelDifferences.size());
        Assert.assertEquals(ChangeType.MODIFY, modelDifferences.get(0).getChangeType());
        Assert.assertEquals(ChangeType.ADD, modelDifferences.get(1).getChangeType());
    }
}
