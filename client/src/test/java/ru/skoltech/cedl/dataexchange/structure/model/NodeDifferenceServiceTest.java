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
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.service.NodeDifferenceService;
import ru.skoltech.cedl.dataexchange.service.impl.ExternalModelDifferenceServiceImpl;
import ru.skoltech.cedl.dataexchange.service.impl.NodeDifferenceServiceImpl;
import ru.skoltech.cedl.dataexchange.service.impl.ParameterDifferenceServiceImpl;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;

import java.util.List;

import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeLocation;
import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeType;

/**
 * Created by D.Knoll on 25.05.2017.
 */
public class NodeDifferenceServiceTest {

    private NodeDifferenceService nodeDifferenceService;
    private SystemModel localSystem;
    private SystemModel remoteSystem;

    @Before
    public void prepare() {
        NodeDifferenceServiceImpl modelDifferenceServiceImpl = new NodeDifferenceServiceImpl();
        modelDifferenceServiceImpl.setParameterDifferenceService(new ParameterDifferenceServiceImpl());
        modelDifferenceServiceImpl.setExternalModelDifferenceService(new ExternalModelDifferenceServiceImpl());
        nodeDifferenceService = modelDifferenceServiceImpl;

        localSystem = new SystemModel();
        localSystem.setName("Sy1");
        localSystem.setLastModification(System.currentTimeMillis() - 5000);

        remoteSystem = new SystemModel();
        remoteSystem.setUuid(localSystem.getUuid());
        remoteSystem.setLastModification(localSystem.getLastModification());
        remoteSystem.setName("Sy1");
    }

    @Test
    public void localNodeAdd() throws Exception {
        SubSystemModel newLocalSub = new SubSystemModel("subsys1");
        localSystem.addSubNode(newLocalSub);

        Assert.assertEquals(1, localSystem.getSubNodes().size());

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG1, md.getChangeLocation());
        Assert.assertEquals(ChangeType.ADD, md.getChangeType());
        Assert.assertEquals(md.getElementPath(), newLocalSub.getNodePath());

        Assert.assertTrue(md.isRevertible());

        md.revertDifference();

        Assert.assertEquals(0, localSystem.getSubNodes().size());

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());

    }

    @Test
    public void localNodeModify() throws Exception {
        SubSystemModel subSystem1 = new SubSystemModel("subSystem1");
        subSystem1.setLastModification(System.currentTimeMillis());
        remoteSystem.addSubNode(subSystem1);
        SubSystemModel subSystem2 = new SubSystemModel();
        Utils.copyBean(subSystem1, subSystem2);
        localSystem.addSubNode(subSystem2);

        subSystem2.setName(subSystem1.getName() + "-v2");

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG1, md.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, md.getChangeType());

        Assert.assertTrue(md.isRevertible());

        md.revertDifference();

        Assert.assertEquals(1, localSystem.getSubNodes().size());
        Assert.assertTrue(localSystem.getSubNodes().get(0).equals(remoteSystem.getSubNodes().get(0)));

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void localNodeRemove() throws Exception {
        SubSystemModel existingRemoteNode = new SubSystemModel("subsystem1");
        existingRemoteNode.setLastModification(remoteSystem.getLastModification() - 100);// parameter it was part of last modification
        remoteSystem.addSubNode(existingRemoteNode);

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG1, md.getChangeLocation());
        Assert.assertEquals(ChangeType.REMOVE, md.getChangeType());

        Assert.assertTrue(md.isRevertible());

        md.revertDifference();

        Assert.assertEquals(1, localSystem.getSubNodes().size());
        Assert.assertTrue(localSystem.getSubNodes().get(0).equals(remoteSystem.getSubNodes().get(0)));

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteNodeAdd() throws Exception {
        SubSystemModel subsystem1 = new SubSystemModel("subsystem1");
        subsystem1.setLastModification(System.currentTimeMillis());
        remoteSystem.addSubNode(subsystem1);

        Assert.assertTrue(localSystem.findLatestModification() < subsystem1.getLastModification());

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG2, md.getChangeLocation());
        Assert.assertEquals(ChangeType.ADD, md.getChangeType());

        Assert.assertTrue(md.isMergeable());
        md.mergeDifference();

        Assert.assertEquals(1, localSystem.getSubNodes().size());
        Assert.assertTrue(localSystem.getSubNodes().get(0).equals(remoteSystem.getSubNodes().get(0)));

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteNodeModify() throws Exception {
        SubSystemModel subsystem1 = new SubSystemModel("subsystem1");
        subsystem1.setLastModification(System.currentTimeMillis() - 1000);
        remoteSystem.addSubNode(subsystem1);
        SubSystemModel subsystem2 = new SubSystemModel();
        Utils.copyBean(subsystem1, subsystem2);
        localSystem.addSubNode(subsystem2);

        subsystem1.setName(subsystem1.getName() + "_v3");
        subsystem1.setLastModification(System.currentTimeMillis());

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG2, md.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, md.getChangeType());

        Assert.assertTrue(md.isMergeable());
        md.mergeDifference();

        Assert.assertEquals(1, localSystem.getSubNodes().size());
        Assert.assertTrue(localSystem.getSubNodes().get(0).equals(remoteSystem.getSubNodes().get(0)));

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteNodeRemove() throws Exception {
        SubSystemModel existingLocalNode = new SubSystemModel("subsystem1");
        existingLocalNode.setLastModification(localSystem.getLastModification() - 100); // parameter was part of last modification
        localSystem.addSubNode(existingLocalNode);

        List<ModelDifference> differences
                = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG2, md.getChangeLocation());
        Assert.assertEquals(ChangeType.REMOVE, md.getChangeType());

        Assert.assertTrue(md.isMergeable());
        md.mergeDifference();

        Assert.assertEquals(0, localSystem.getSubNodes().size());

        differences = nodeDifferenceService.computeNodeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }


}
