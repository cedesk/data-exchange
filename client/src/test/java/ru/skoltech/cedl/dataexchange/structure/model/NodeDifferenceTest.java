package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.NodeDifference;

import java.util.List;

import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeLocation;
import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeType;

/**
 * Created by D.Knoll on 25.05.2017.
 */
public class NodeDifferenceTest {

    private SystemModel localSystem;
    private SystemModel remoteSystem;

    @Test
    public void localNodeAdd() {
        SubSystemModel newLocalSub = new SubSystemModel("subsys1");
        localSystem.addSubNode(newLocalSub);

        Assert.assertEquals(1, localSystem.getSubNodes().size());

        List<ModelDifference> differences;
        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG1, md.getChangeLocation());
        Assert.assertEquals(ChangeType.ADD, md.getChangeType());
        Assert.assertEquals(md.getNodeName(), newLocalSub.getNodePath());

        Assert.assertTrue(md.isRevertible());

        md.revertDifference();

        Assert.assertEquals(0, localSystem.getSubNodes().size());

        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());

    }

    @Test
    public void localNodeModify() {
        SubSystemModel subSystem1 = new SubSystemModel("subSystem1");
        subSystem1.setLastModification(System.currentTimeMillis());
        remoteSystem.addSubNode(subSystem1);
        SubSystemModel subSystem2 = new SubSystemModel();
        Utils.copyBean(subSystem1, subSystem2);
        localSystem.addSubNode(subSystem2);

        subSystem2.setName(subSystem1.getName() + "-v2");

        List<ModelDifference> differences;
        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG1, md.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, md.getChangeType());

        Assert.assertTrue(md.isRevertible());

        md.revertDifference();

        Assert.assertEquals(1, localSystem.getSubNodes().size());
        Assert.assertTrue(localSystem.getSubNodes().get(0).equals(remoteSystem.getSubNodes().get(0)));

        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void localNodeRemove() {
        SubSystemModel existingRemoteNode = new SubSystemModel("subsystem1");
        existingRemoteNode.setLastModification(remoteSystem.getLastModification() - 100);// parameter it was part of last modification
        remoteSystem.addSubNode(existingRemoteNode);

        List<ModelDifference> differences;
        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG1, md.getChangeLocation());
        Assert.assertEquals(ChangeType.REMOVE, md.getChangeType());

        Assert.assertTrue(md.isRevertible());

        md.revertDifference();

        Assert.assertEquals(1, localSystem.getSubNodes().size());
        Assert.assertTrue(localSystem.getSubNodes().get(0).equals(remoteSystem.getSubNodes().get(0)));

        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Before
    public void prepare() {
        localSystem = new SystemModel();
        localSystem.setName("Sy1");
        localSystem.setLastModification(System.currentTimeMillis() - 5000);

        remoteSystem = new SystemModel();
        remoteSystem.setUuid(localSystem.getUuid());
        remoteSystem.setLastModification(localSystem.getLastModification());
        remoteSystem.setName("Sy1");
    }

    @Test
    public void remoteNodeAdd() {
        SubSystemModel subsystem1 = new SubSystemModel("subsystem1");
        subsystem1.setLastModification(System.currentTimeMillis());
        remoteSystem.addSubNode(subsystem1);

        Assert.assertTrue(localSystem.findLatestModification() < subsystem1.getLastModification());

        List<ModelDifference> differences;
        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG2, md.getChangeLocation());
        Assert.assertEquals(ChangeType.ADD, md.getChangeType());

        Assert.assertTrue(md.isMergeable());
        md.mergeDifference();

        Assert.assertEquals(1, localSystem.getSubNodes().size());
        Assert.assertTrue(localSystem.getSubNodes().get(0).equals(remoteSystem.getSubNodes().get(0)));

        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteNodeModify() {
        SubSystemModel subsystem1 = new SubSystemModel("subsystem1");
        subsystem1.setLastModification(System.currentTimeMillis());
        remoteSystem.addSubNode(subsystem1);
        SubSystemModel subsystem2 = new SubSystemModel();
        Utils.copyBean(subsystem1, subsystem2);
        localSystem.addSubNode(subsystem2);

        subsystem1.setName(subsystem1.getName() + "_v3");
        subsystem1.setLastModification(System.currentTimeMillis());

        List<ModelDifference> differences;
        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG2, md.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, md.getChangeType());

        Assert.assertTrue(md.isMergeable());
        md.mergeDifference();

        Assert.assertEquals(1, localSystem.getSubNodes().size());
        Assert.assertTrue(localSystem.getSubNodes().get(0).equals(remoteSystem.getSubNodes().get(0)));

        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteNodeRemove() {
        SubSystemModel existingLocalNode = new SubSystemModel("subsystem1");
        existingLocalNode.setLastModification(localSystem.getLastModification() - 100); // parameter was part of last modification
        localSystem.addSubNode(existingLocalNode);

        List<ModelDifference> differences;
        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG2, md.getChangeLocation());
        Assert.assertEquals(ChangeType.REMOVE, md.getChangeType());

        Assert.assertTrue(md.isMergeable());
        md.mergeDifference();

        Assert.assertEquals(0, localSystem.getSubNodes().size());

        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }


}
