package ru.skoltech.cedl.dataexchange.structure.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ParameterDifference;

import java.util.List;

import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeType;

/**
 * Created by D.Knoll on 25.05.2017.
 */
public class ParameterDifferenceTest {

    private SystemModel localSystem;
    private SystemModel remoteSystem;

    @Test
    public void localParameterAdd() {
        ParameterModel newLocalParam = new ParameterModel("param1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        localSystem.addParameter(newLocalParam);

        Assert.assertEquals(1, localSystem.getParameters().size());

        List<ParameterDifference> differences;
        differences = ParameterDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG1, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.ADD, pd.getChangeType());

        Assert.assertTrue(pd.isRevertible());

        pd.revertDifference();

        Assert.assertEquals(0, localSystem.getParameters().size());

        differences = ParameterDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());

    }

    @Test
    public void localParameterModify() {
        ParameterModel param1 = new ParameterModel("param1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        param1.setLastModification(System.currentTimeMillis());
        remoteSystem.addParameter(param1);
        ParameterModel param2 = new ParameterModel();
        Utils.copyBean(param1, param2);
        localSystem.addParameter(param2);

        param2.setValue(param1.getValue() * 2);

        List<ParameterDifference> differences;
        differences = ParameterDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG1, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, pd.getChangeType());

        Assert.assertTrue(pd.isRevertible());

        pd.revertDifference();

        Assert.assertEquals(1, localSystem.getParameters().size());
        Assert.assertTrue(localSystem.getParameters().get(0).equals(remoteSystem.getParameters().get(0)));

        differences = ParameterDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void localParameterRemove() {
        ParameterModel existingRemoteParam = new ParameterModel("param1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        existingRemoteParam.setLastModification(remoteSystem.getLastModification() - 100);// parameter it was part of last modification
        remoteSystem.addParameter(existingRemoteParam);

        List<ParameterDifference> differences;
        differences = ParameterDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG1, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.REMOVE, pd.getChangeType());

        Assert.assertTrue(pd.isRevertible());

        pd.revertDifference();

        Assert.assertEquals(1, localSystem.getParameters().size());
        Assert.assertTrue(localSystem.getParameters().get(0).equals(remoteSystem.getParameters().get(0)));

        differences = ParameterDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Before
    public void prepare() {
        localSystem = new SystemModel();
        localSystem.setName("S-1");
        localSystem.setLastModification(System.currentTimeMillis() - 5000);

        remoteSystem = new SystemModel();
        remoteSystem.setUuid(localSystem.getUuid());
        remoteSystem.setLastModification(localSystem.getLastModification());
        remoteSystem.setName("S-2");
    }

    @Test
    public void remoteParameterAdd() {
        ParameterModel paramRemote = new ParameterModel("param1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        paramRemote.setLastModification(System.currentTimeMillis());
        remoteSystem.addParameter(paramRemote);

        Assert.assertTrue(localSystem.findLatestModification() < paramRemote.getLastModification());

        List<ParameterDifference> differences;
        differences = ParameterDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG2, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.ADD, pd.getChangeType());

        Assert.assertTrue(pd.isMergeable());
        pd.mergeDifference();

        Assert.assertEquals(1, localSystem.getParameters().size());
        Assert.assertTrue(localSystem.getParameters().get(0).equals(remoteSystem.getParameters().get(0)));

        differences = ParameterDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteParameterModify() {
        ParameterModel param1 = new ParameterModel("param1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        param1.setLastModification(System.currentTimeMillis());
        remoteSystem.addParameter(param1);
        ParameterModel param2 = new ParameterModel();
        Utils.copyBean(param1, param2);
        localSystem.addParameter(param2);

        param1.setValue(param1.getValue() * 2);
        param1.setLastModification(System.currentTimeMillis());

        List<ParameterDifference> differences;
        differences = ParameterDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG2, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, pd.getChangeType());

        Assert.assertTrue(pd.isMergeable());
        pd.mergeDifference();

        Assert.assertEquals(1, localSystem.getParameters().size());
        Assert.assertTrue(localSystem.getParameters().get(0).equals(remoteSystem.getParameters().get(0)));

        differences = ParameterDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteParameterRemove() {
        ParameterModel existingLocalParam = new ParameterModel("param1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        existingLocalParam.setLastModification(localSystem.getLastModification() - 100); // parameter was part of last modification
        localSystem.addParameter(existingLocalParam);

        List<ParameterDifference> differences;
        differences = ParameterDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG2, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.REMOVE, pd.getChangeType());

        Assert.assertTrue(pd.isMergeable());
        pd.mergeDifference();

        Assert.assertEquals(0, localSystem.getParameters().size());

        differences = ParameterDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }


}
