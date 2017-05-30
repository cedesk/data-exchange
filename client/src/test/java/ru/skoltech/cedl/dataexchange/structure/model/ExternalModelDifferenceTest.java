package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.NodeDifference;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeLocation;
import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeType;

/**
 * Created by D.Knoll on 25.05.2017.
 */
public class ExternalModelDifferenceTest {

    private SystemModel localSystem;
    private SystemModel remoteSystem;
    private ExternalModel extMod;

    @Test
    public void localNodeAdd() throws IOException {
        // extMod.setLastModification(System.currentTimeMillis()); // is already initialized by reading from file
        localSystem.addExternalModel(extMod);

        Assert.assertEquals(1, localSystem.getExternalModels().size());

        List<ModelDifference> differences;
        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG1, md.getChangeLocation());
        Assert.assertEquals(ChangeType.ADD, md.getChangeType());
        Assert.assertEquals(md.getElementPath(), extMod.getNodePath());

        Assert.assertTrue(md.isRevertible());

        md.revertDifference();

        Assert.assertEquals(0, localSystem.getExternalModels().size());

        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());

    }

    @Test
    public void localNodeModify() {
        ExternalModel extModel1 = extMod;
        extModel1.setLastModification(System.currentTimeMillis());
        remoteSystem.addExternalModel(extModel1);
        ExternalModel extModel2 = new ExternalModel();
        Utils.copyBean(extModel1, extModel2);
        localSystem.addExternalModel(extModel2);

        extModel2.setName(extModel1.getName() + "-v2");

        List<ModelDifference> differences;
        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG1, md.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, md.getChangeType());

        Assert.assertTrue(md.isRevertible());

        md.revertDifference();

        Assert.assertEquals(1, localSystem.getExternalModels().size());
        Assert.assertTrue(localSystem.getExternalModels().get(0).equals(remoteSystem.getExternalModels().get(0)));
        Assert.assertTrue(localSystem.getExternalModels().get(0).getParent() == localSystem);

        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void localNodeRemove() {
        ExternalModel existingRemoteNode = extMod;
        existingRemoteNode.setLastModification(remoteSystem.getLastModification() - 100);// parameter it was part of last modification
        remoteSystem.addExternalModel(existingRemoteNode);

        Assert.assertEquals(1, remoteSystem.getExternalModels().size());
        Assert.assertEquals(0, localSystem.getExternalModels().size());

        List<ModelDifference> differences;
        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG1, md.getChangeLocation());
        Assert.assertEquals(ChangeType.REMOVE, md.getChangeType());

        Assert.assertTrue(md.isRevertible());

        md.revertDifference();

        Assert.assertEquals(1, localSystem.getExternalModels().size());
        Assert.assertTrue(localSystem.getExternalModels().get(0).equals(remoteSystem.getExternalModels().get(0)));

        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Before
    public void prepare() throws IOException, URISyntaxException {
        localSystem = new SystemModel();
        localSystem.setName("Sy1");
        localSystem.setLastModification(System.currentTimeMillis() - 5000);

        remoteSystem = new SystemModel();
        remoteSystem.setUuid(localSystem.getUuid());
        remoteSystem.setLastModification(localSystem.getLastModification());
        remoteSystem.setName("Sy1");

        File externalModelFile = new File(this.getClass().getResource("/attachment.xls").toURI());
        extMod = ExternalModelFileHandler.newFromFile(externalModelFile, localSystem);
    }

    @Test
    public void remoteNodeAdd() {
        ExternalModel extModel1 = extMod;
        extModel1.setLastModification(System.currentTimeMillis());
        remoteSystem.addExternalModel(extModel1);

        Assert.assertTrue(localSystem.findLatestModification() < extModel1.getLastModification());

        List<ModelDifference> differences;
        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG2, md.getChangeLocation());
        Assert.assertEquals(ChangeType.ADD, md.getChangeType());

        Assert.assertTrue(md.isMergeable());
        md.mergeDifference();

        Assert.assertEquals(1, localSystem.getExternalModels().size());
        Assert.assertTrue(localSystem.getExternalModels().get(0).equals(remoteSystem.getExternalModels().get(0)));

        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteNodeModify() {
        ExternalModel extModel1 = extMod;
        remoteSystem.addExternalModel(extModel1);
        ExternalModel extModel2 = new ExternalModel();
        Utils.copyBean(extModel1, extModel2);
        localSystem.addExternalModel(extModel2);

        extModel1.setName(extModel1.getName() + "_v3");
        extModel1.setLastModification(System.currentTimeMillis());

        List<ModelDifference> differences;
        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG2, md.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, md.getChangeType());

        Assert.assertTrue(md.isMergeable());
        md.mergeDifference();

        Assert.assertEquals(1, localSystem.getExternalModels().size());
        Assert.assertTrue(localSystem.getExternalModels().get(0).equals(remoteSystem.getExternalModels().get(0)));
        Assert.assertTrue(localSystem.getExternalModels().get(0).getParent() == localSystem);

        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    //TODO: enable
    //@Test
    public void remoteNodeRemove() {
        ExternalModel existingLocalNode = extMod;
        existingLocalNode.setLastModification(localSystem.getLastModification() - 100); // parameter was part of last modification
        localSystem.addExternalModel(existingLocalNode);

        List<ModelDifference> differences;
        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ModelDifference md = differences.get(0);
        Assert.assertEquals(ChangeLocation.ARG2, md.getChangeLocation());
        Assert.assertEquals(ChangeType.REMOVE, md.getChangeType());

        Assert.assertTrue(md.isMergeable());
        md.mergeDifference();

        Assert.assertEquals(0, localSystem.getExternalModels().size());

        differences = NodeDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }


}
