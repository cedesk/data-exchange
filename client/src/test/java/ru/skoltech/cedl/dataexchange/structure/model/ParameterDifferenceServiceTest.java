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
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.revision.SystemModelRepository;
import ru.skoltech.cedl.dataexchange.service.ParameterDifferenceService;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ParameterDifference;

import java.util.List;

import static ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference.ChangeType;

/**
 * Created by D.Knoll on 25.05.2017.
 */
public class ParameterDifferenceServiceTest extends AbstractApplicationContextTest {

    private ParameterDifferenceService parameterDifferenceService;
    private SystemModelRepository systemModelRepository;

    private SystemModel baseSystemModel;

    @Before
    public void prepare() {
        parameterDifferenceService = context.getBean(ParameterDifferenceService.class);
        systemModelRepository = context.getBean(SystemModelRepository.class);

        baseSystemModel = new SystemModel();
        baseSystemModel.setName("SM");
    }

    @Test
    public void localParameterAdd() throws Exception {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        int currentRevisionNumber = localSystem.getRevision();

        ParameterModel newLocalParam = new ParameterModel("param1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        localSystem.addParameter(newLocalParam);

        Assert.assertEquals(1, localSystem.getParameters().size());

        List<ParameterDifference> differences =
                parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG1, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.ADD, pd.getChangeType());

        Assert.assertTrue(pd.isRevertible());

        pd.revertDifference();

        Assert.assertEquals(0, localSystem.getParameters().size());

        differences = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void localParameterModify() throws Exception {
        ParameterModel parameterModel = new ParameterModel("param", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);

        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        localSystem.addParameter(parameterModel);
        localSystem = systemModelRepository.saveAndFlush(localSystem);

        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        int currentRevisionNumber = localSystem.getRevision();

        parameterModel = localSystem.getParameters().get(0);
        parameterModel.setValue(parameterModel.getValue() * 2);

        List<ParameterDifference> differences =
                parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG1, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, pd.getChangeType());

        Assert.assertTrue(pd.isRevertible());

        pd.revertDifference();

        Assert.assertEquals(1, localSystem.getParameters().size());
        Assert.assertTrue(localSystem.getParameters().get(0).equals(remoteSystem.getParameters().get(0)));
        Assert.assertTrue(localSystem.getParameters().get(0).getParent() == localSystem);

        differences = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void localParameterModifyLink() throws Exception {
        ParameterModel param1 = new ParameterModel("param1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        ParameterModel param2 = new ParameterModel("param2", 13.0, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        param1.setNature(ParameterNature.INPUT);
        param1.setValueSource(ParameterValueSource.LINK);
        param1.setValueLink(param2); // unrealistic that paramR1 and paramR2 belong to the same system node

        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        localSystem.addParameter(param1);
        localSystem.addParameter(param2);
        localSystem = systemModelRepository.saveAndFlush(localSystem);

        ParameterModel paramL1 = localSystem.getParameters().get(0);
        ParameterModel paramL2 = localSystem.getParameters().get(1);

        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        int currentRevisionNumber = localSystem.getRevision();

        ParameterModel paramR1 = remoteSystem.getParameters().get(0);
        ParameterModel paramR2 = remoteSystem.getParameters().get(1);

        ParameterModel paramLx = new ParameterModel("paramLx", 51.0, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        paramLx.setUuid(paramR1.getUuid());
        paramL1.setValueLink(paramLx);
        Assert.assertNotEquals(remoteSystem.getParameters().get(0).getValueLink(), localSystem.getParameters().get(0).getValueLink());


        List<ParameterDifference> differences
                = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        System.out.println(pd);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG1, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, pd.getChangeType());
        Assert.assertEquals("param1", pd.getParameter1().getName());

        Assert.assertTrue(pd.isRevertible());
        pd.revertDifference();

        Assert.assertEquals(2, localSystem.getParameters().size());
        Assert.assertEquals(localSystem.getParameters().get(0), remoteSystem.getParameters().get(0));
        Assert.assertEquals(paramR1, localSystem.getParameters().get(0));
        Assert.assertEquals(paramL1, localSystem.getParameters().get(0));
        Assert.assertEquals(paramR2, localSystem.getParameters().get(1));
        Assert.assertEquals(paramL2, localSystem.getParameters().get(1));
        Assert.assertEquals(paramR2, localSystem.getParameters().get(0).getValueLink());

        differences = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void localParameterRemove() throws Exception {
        ParameterModel existingRemoteParam = new ParameterModel("param1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);

        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        localSystem.addParameter(existingRemoteParam);
        localSystem = systemModelRepository.saveAndFlush(localSystem);

        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        int currentRevisionNumber = localSystem.getRevision();

        localSystem.getParameters().clear();

        List<ParameterDifference> differences
                = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG1, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.REMOVE, pd.getChangeType());

        Assert.assertTrue(pd.isRevertible());

        pd.revertDifference();

        Assert.assertEquals(1, localSystem.getParameters().size());
        Assert.assertTrue(localSystem.getParameters().get(0).equals(remoteSystem.getParameters().get(0)));

        differences = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteParameterAdd() throws Exception {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);
        SystemModel remoteSystem = systemModelRepository.findOne(localSystem.getId());
        int currentRevisionNumber = localSystem.getRevision();

        ParameterModel paramRemote = new ParameterModel("param1", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        remoteSystem.addParameter(paramRemote);
        remoteSystem = systemModelRepository.saveAndFlush(remoteSystem);

        int lastRevisionNumber = remoteSystem.getRevision();
        Assert.assertTrue(currentRevisionNumber < lastRevisionNumber);

        List<ParameterDifference> differences
                = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG2, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.ADD, pd.getChangeType());

        Assert.assertTrue(pd.isMergeable());
        pd.mergeDifference();

        Assert.assertEquals(1, localSystem.getParameters().size());
        Assert.assertTrue(localSystem.getParameters().get(0).equals(remoteSystem.getParameters().get(0)));

        differences = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteParameterModify() throws Exception {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);

        ParameterModel param = new ParameterModel("param", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        localSystem.addParameter(param);
        localSystem = systemModelRepository.saveAndFlush(localSystem);

        int currentRevisionNumber = localSystem.getRevision();

        SystemModel remoteSystem = systemModelRepository.saveAndFlush(localSystem);
        param = remoteSystem.getParameters().get(0);
        param.setValue(param.getValue() * 2);
        remoteSystem = systemModelRepository.saveAndFlush(remoteSystem);

        List<ParameterDifference> differences
                = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG2, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.MODIFY, pd.getChangeType());

        Assert.assertTrue(pd.isMergeable());
        pd.mergeDifference();

        Assert.assertEquals(1, localSystem.getParameters().size());
        Assert.assertTrue(localSystem.getParameters().get(0).equals(remoteSystem.getParameters().get(0)));

        differences = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(0, differences.size());
    }

    @Test
    public void remoteParameterRemove() throws Exception {
        SystemModel localSystem = systemModelRepository.saveAndFlush(baseSystemModel);

        ParameterModel existingLocalParam = new ParameterModel("param", 0.4, ParameterNature.INPUT, ParameterValueSource.MANUAL);
        localSystem.addParameter(existingLocalParam);
        localSystem = systemModelRepository.saveAndFlush(localSystem);

        int currentRevisionNumber = localSystem.getRevision();

        SystemModel remoteSystem = systemModelRepository.saveAndFlush(localSystem);
        remoteSystem.getParameters().clear();

        remoteSystem = systemModelRepository.saveAndFlush(remoteSystem);

        List<ParameterDifference> differences
                = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(1, differences.size());
        ParameterDifference pd = differences.get(0);
        Assert.assertEquals(ModelDifference.ChangeLocation.ARG2, pd.getChangeLocation());
        Assert.assertEquals(ChangeType.REMOVE, pd.getChangeType());

        Assert.assertTrue(pd.isMergeable());
        pd.mergeDifference();

        Assert.assertEquals(0, localSystem.getParameters().size());

        differences = parameterDifferenceService.computeParameterDifferences(localSystem, remoteSystem, currentRevisionNumber);
        Assert.assertEquals(0, differences.size());
    }

}
