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

package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterRevision;
import ru.skoltech.cedl.dataexchange.entity.calculation.Argument;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.calculation.operation.Operation;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.revision.SystemModelRepository;
import ru.skoltech.cedl.dataexchange.service.ParameterModelService;
import ru.skoltech.cedl.dataexchange.structure.BasicSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by D.Knoll on 23.06.2015.
 */
public class ParameterModelServiceTest extends AbstractApplicationContextTest {

    private SystemBuilder systemBuilder;
    private ParameterModelService parameterModelService;
    private SystemModelRepository systemModelRepository;

    private static final String ADMIN = "admin";
    private SystemModel systemModel;
    private ParameterModel parameterModel;
    private ExternalModelReference valueReference, exportReference;
    private ExternalModel importExternalModel, exportExternalModel;
    private Operation operation;
    private Argument argument1, argument2;

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        systemBuilder = context.getBean(BasicSpaceSystemBuilder.class);
        systemBuilder.modelDepth(1);

        parameterModelService = context.getBean(ParameterModelService.class);
        systemModelRepository = context.getBean(SystemModelRepository.class);

        systemModel = systemBuilder.build("testModel");
        systemModel = systemModelRepository.saveAndFlush(systemModel);

        importExternalModel = mock(ExternalModel.class);
        when(importExternalModel.getName()).thenReturn("importExternalModel");
        when(importExternalModel.getUuid()).thenReturn("1111");
        exportExternalModel = mock(ExternalModel.class);
        when(exportExternalModel.getName()).thenReturn("exportExternalModel");
        when(exportExternalModel.getUuid()).thenReturn("2222");
        systemModel.addExternalModel(importExternalModel);
        systemModel.addExternalModel(exportExternalModel);

        valueReference = new ExternalModelReference();
        valueReference.setTarget("importTarget");
        valueReference.setExternalModel(importExternalModel);

        exportReference = new ExternalModelReference();
        exportReference.setTarget("exportTarget");
        exportReference.setExternalModel(exportExternalModel);

        operation = mock(Operation.class);
        argument1 = mock(Argument.class);
        argument2 = mock(Argument.class);
        Calculation calculation = new Calculation();
        calculation.setOperation(operation);
        calculation.setArguments(Arrays.asList(argument1, argument2));

        parameterModel = systemModel.getParameters().get(0);
        parameterModel.setValueReference(valueReference);
        parameterModel.setExportReference(exportReference);
        parameterModel.setImportModel(importExternalModel);
        parameterModel.setExportModel(exportExternalModel);
        parameterModel.setCalculation(calculation);
    }

    @Test(expected = NullPointerException.class)
    public void testCloneParameterModelFail1() {
        parameterModelService.cloneParameterModel(null, new ParameterModel());
    }

    @Test(expected = NullPointerException.class)
    public void testCloneParameterModelFail2() {
        parameterModelService.cloneParameterModel("name", null);
    }

    @Test
    public void testCloneParameterModel() {
        String name = "name";
        ParameterModel newParameterModel1 = parameterModelService.cloneParameterModel(name, parameterModel);
        ParameterModel newParameterModel2 = parameterModelService.cloneParameterModel(name, parameterModel, systemModel);

        this.checkCloneParameterModel(newParameterModel1, name);
        this.checkCloneParameterModel(newParameterModel2, name);
    }

    @Test(expected = NullPointerException.class)
    public void testCloneParameterModelFail4() {
        parameterModelService.cloneParameterModel("name", null, systemModel);
    }

    @Test(expected = NullPointerException.class)
    public void testCloneParameterModelFail5() {
        parameterModelService.cloneParameterModel("name", new ParameterModel(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testCloneParameterModelFail3() {
        parameterModelService.cloneParameterModel(null, new ParameterModel(), systemModel);
    }

    @Test
    public void testParameterModelChangeHistory() {
        //ParameterModel newParameterModel = new ParameterModel("new-parameter-A", 3.1415);
        //systemModel.addParameter(newParameterModel);

        SystemModel systemModel = systemBuilder.build("testModel");
        systemModel = systemModelRepository.saveAndFlush(systemModel);
        ParameterModel parameterModel = systemModel.getParameters().get(0);
        parameterModel.setName("parameter-1-renamed");
        systemModelRepository.saveAndFlush(systemModel);

        List<ParameterRevision> changeHistory = parameterModelService.parameterModelChangeHistory(parameterModel);

        Assert.assertEquals(2, changeHistory.size());
        Assert.assertEquals(ADMIN, changeHistory.get(0).getRevisionAuthor());
    }

    private void checkCloneParameterModel(ParameterModel clonedParameterModel, String name) {
        assertNotNull(clonedParameterModel);
        assertNotEquals(parameterModel, clonedParameterModel);
        assertNotEquals(parameterModel.getUuid(), clonedParameterModel.getUuid());
        assertEquals(name, clonedParameterModel.getName());
        assertEquals(parameterModel.getValue(), clonedParameterModel.getValue());
        assertEquals(parameterModel.getUnit(), clonedParameterModel.getUnit());
        assertEquals(parameterModel.getNature(), clonedParameterModel.getNature());
        assertEquals(parameterModel.getValueSource(), clonedParameterModel.getValueSource());
        assertFalse(parameterModel.getValueReference() == clonedParameterModel.getValueReference());
        assertEquals(valueReference, clonedParameterModel.getValueReference());
        assertEquals(parameterModel.getValueReference().getTarget(), clonedParameterModel.getValueReference().getTarget());
        assertEquals(importExternalModel, clonedParameterModel.getValueReference().getExternalModel());
        assertEquals(parameterModel.getValueLink(), clonedParameterModel.getValueLink());
        assertEquals(parameterModel.getCalculation(), clonedParameterModel.getCalculation());
        assertEquals(importExternalModel, clonedParameterModel.getImportModel());
        assertEquals(parameterModel.getImportField(), clonedParameterModel.getImportField());
        assertEquals(parameterModel.getIsReferenceValueOverridden(), clonedParameterModel.getIsReferenceValueOverridden());
        assertEquals(parameterModel.getIsExported(), clonedParameterModel.getIsExported());
        assertFalse(parameterModel.getExportReference() == clonedParameterModel.getExportReference());
        assertEquals(exportReference, clonedParameterModel.getExportReference());
        assertEquals(parameterModel.getExportReference().getTarget(), clonedParameterModel.getExportReference().getTarget());
        assertEquals(exportExternalModel, clonedParameterModel.getExportReference().getExternalModel());
        assertEquals(exportExternalModel, clonedParameterModel.getExportModel());
        assertEquals(parameterModel.getExportField(), clonedParameterModel.getExportField());
        assertEquals(parameterModel.getDescription(), clonedParameterModel.getDescription());
        assertEquals(parameterModel.getLastModification(), clonedParameterModel.getLastModification());
        assertFalse(parameterModel.getCalculation() == clonedParameterModel.getCalculation());
        assertEquals(parameterModel.getCalculation(), clonedParameterModel.getCalculation());
        assertEquals(parameterModel.getCalculation().getOperation(), clonedParameterModel.getCalculation().getOperation());
        assertEquals(operation, clonedParameterModel.getCalculation().getOperation());
        assertEquals(parameterModel.getCalculation().getArguments(), clonedParameterModel.getCalculation().getArguments());
        assertEquals(2, clonedParameterModel.getCalculation().getArguments().size());
        assertThat(parameterModel.getCalculation().getArguments(), hasItem(argument1));
        assertThat(parameterModel.getCalculation().getArguments(), hasItem(argument2));
        assertEquals(systemModel, clonedParameterModel.getParent());
    }
}
