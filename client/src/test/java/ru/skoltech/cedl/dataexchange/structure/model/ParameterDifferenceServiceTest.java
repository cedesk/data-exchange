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
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.service.ParameterDifferenceService;
import ru.skoltech.cedl.dataexchange.service.impl.ParameterDifferenceServiceImpl;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ParameterDifference;

import java.util.List;

import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeType;

/**
 * Created by D.Knoll on 25.05.2017.
 */
public class ParameterDifferenceServiceTest {

    private ParameterDifferenceService parameterDifferenceService;
    private SystemModel localSystem;
    private SystemModel remoteSystem;

    @Before
    public void prepare() {
        parameterDifferenceService = new ParameterDifferenceServiceImpl();

        localSystem = new SystemModel();
        localSystem.setName("S-1");
        localSystem.setLastModification(System.currentTimeMillis() - 5000);

        remoteSystem = new SystemModel();
        remoteSystem.setUuid(localSystem.getUuid());
        remoteSystem.setLastModification(localSystem.getLastModification());
        remoteSystem.setName("S-2");
    }

    @Test
    public void localParameterAdd() throws Exception {
        ParameterModel newLocalParam = new ParameterModel("param1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        localSystem.addParameter(newLocalParam);

        Assert.assertEquals(1, localSystem.getParameters().size());

        List<ParameterDifference> differences =
                parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG1, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.ADD, pd.getChangeType());

        Assert.assertTrue(pd.isRevertible());

        pd.revertDifference();

        Assert.assertEquals(0, localSystem.getParameters().size());

        differences = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
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

        List<ParameterDifference> differences =
                parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG1, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, pd.getChangeType());

        Assert.assertTrue(pd.isRevertible());

        pd.revertDifference();

        Assert.assertEquals(1, localSystem.getParameters().size());
        Assert.assertTrue(localSystem.getParameters().get(0).equals(remoteSystem.getParameters().get(0)));
        Assert.assertTrue(localSystem.getParameters().get(0).getParent() == localSystem);

        differences = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
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

        List<ParameterDifference> differences
                = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
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

        differences = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void localParameterRemove() throws Exception {
        ParameterModel existingRemoteParam = new ParameterModel("param1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        existingRemoteParam.setLastModification(remoteSystem.getLastModification() - 100);// parameter it was part of last modification
        remoteSystem.addParameter(existingRemoteParam);

        List<ParameterDifference> differences
                = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG1, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.REMOVE, pd.getChangeType());

        Assert.assertTrue(pd.isRevertible());

        pd.revertDifference();

        Assert.assertEquals(1, localSystem.getParameters().size());
        Assert.assertTrue(localSystem.getParameters().get(0).equals(remoteSystem.getParameters().get(0)));

        differences = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteParameterAdd() throws Exception {
        ParameterModel paramRemote = new ParameterModel("param1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        paramRemote.setLastModification(System.currentTimeMillis());
        remoteSystem.addParameter(paramRemote);

        Assert.assertTrue(localSystem.findLatestModification() < paramRemote.getLastModification());

        List<ParameterDifference> differences
                = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG2, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.ADD, pd.getChangeType());

        Assert.assertTrue(pd.isMergeable());
        pd.mergeDifference();

        Assert.assertEquals(1, localSystem.getParameters().size());
        Assert.assertTrue(localSystem.getParameters().get(0).equals(remoteSystem.getParameters().get(0)));

        differences = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
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

        List<ParameterDifference> differences
                = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG2, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, pd.getChangeType());

        Assert.assertTrue(pd.isMergeable());
        pd.mergeDifference();

        Assert.assertEquals(1, localSystem.getParameters().size());
        Assert.assertTrue(localSystem.getParameters().get(0).equals(remoteSystem.getParameters().get(0)));

        differences = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteParameterRemove() throws Exception {
        ParameterModel existingLocalParam = new ParameterModel("param1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        existingLocalParam.setLastModification(localSystem.getLastModification() - 100); // parameter was part of last modification
        localSystem.addParameter(existingLocalParam);

        List<ParameterDifference> differences
                = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG2, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.REMOVE, pd.getChangeType());

        Assert.assertTrue(pd.isMergeable());
        pd.mergeDifference();

        Assert.assertEquals(0, localSystem.getParameters().size());

        differences = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, localSystem.findLatestModification());
        Assert.assertEquals(0, differences.size());
    }


}
