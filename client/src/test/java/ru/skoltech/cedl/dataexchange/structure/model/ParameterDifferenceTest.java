/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

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
    public void localParameterAdd() throws Exception {
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
    public void localParameterModify() throws Exception {
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
        Assert.assertTrue(localSystem.getParameters().get(0).getParent() == localSystem);


        differences = ParameterDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void localParameterModifyLink() throws Exception {
        ParameterModel paramR1 = new ParameterModel("paramR1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        paramR1.setLastModification(System.currentTimeMillis());
        remoteSystem.addParameter(paramR1);
        ParameterModel paramR2 = new ParameterModel("paramR2", 13.0, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        paramR2.setLastModification(System.currentTimeMillis());
        remoteSystem.addParameter(paramR2);
        paramR1.setNature(ParameterNature.INPUT);
        paramR1.setValueSource(ParameterValueSource.LINK);
        paramR1.setValueLink(paramR2); // unrealistic that paramR1 and paramR2 belong to the same system node

        ParameterModel paramL1 = new ParameterModel();
        Utils.copyBean(paramR1, paramL1);
        localSystem.addParameter(paramL1);
        ParameterModel paramL2 = new ParameterModel();
        Utils.copyBean(paramR2, paramL2);
        localSystem.addParameter(paramL2);

        ParameterModel paramLx = new ParameterModel("paramLx", 51.0, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        paramLx.setUuid(paramR1.getUuid());
        paramL1.setValueLink(paramLx);
        Assert.assertNotEquals(remoteSystem.getParameters().get(0).getValueLink(), localSystem.getParameters().get(0).getValueLink());

        List<ParameterDifference> differences;
        differences = ParameterDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        System.out.println(pd);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG1, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, pd.getChangeType());
        Assert.assertEquals("paramR1", pd.getParameter1().getName());

        Assert.assertTrue(pd.isRevertible());
        pd.revertDifference();

        Assert.assertEquals(2, localSystem.getParameters().size());
        Assert.assertEquals(localSystem.getParameters().get(0), remoteSystem.getParameters().get(0));
        Assert.assertEquals(paramR1, localSystem.getParameters().get(0));
        Assert.assertEquals(paramL1, localSystem.getParameters().get(0));
        Assert.assertEquals(paramR2, localSystem.getParameters().get(1));
        Assert.assertEquals(paramL2, localSystem.getParameters().get(1));
        Assert.assertEquals(paramR2, localSystem.getParameters().get(0).getValueLink());

        differences = ParameterDifference.computeDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void localParameterRemove() throws Exception {
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
    public void remoteParameterAdd() throws Exception {
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
    public void remoteParameterModify() throws Exception {
        ParameterModel param1 = new ParameterModel("param1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        param1.setLastModification(System.currentTimeMillis() - 100);
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
    public void remoteParameterRemove() throws Exception {
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
