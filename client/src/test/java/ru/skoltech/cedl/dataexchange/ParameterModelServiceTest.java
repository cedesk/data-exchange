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

/**
 * Created by D.Knoll on 23.06.2015.
 */
public class ParameterModelServiceTest extends AbstractApplicationContextTest {

    private static final String ADMIN = "admin";
    private SystemModel systemModel;
    private ParameterModel parameterModel;

    private ParameterModelService parameterModelService;
    private SystemModelRepository systemModelRepository;

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        SystemBuilder systemBuilder = context.getBean(BasicSpaceSystemBuilder.class);
        systemBuilder.modelDepth(1);

        parameterModelService = context.getBean(ParameterModelService.class);
        systemModelRepository = context.getBean(SystemModelRepository.class);

        systemModel = systemBuilder.build("testModel");
        systemModel = systemModelRepository.saveAndFlush(systemModel);

        parameterModel = systemModel.getParameters().get(0);
    }

    @Test
    public void testCloneParameterModel() {
        ExternalModel importExportExternalModel = mock(ExternalModel.class);
        ExternalModel exportImportExternalModel = mock(ExternalModel.class);

        ExternalModelReference valueReference = new ExternalModelReference();
        valueReference.setTarget("importTarget");
        valueReference.setExternalModel(importExportExternalModel);

        ExternalModelReference exportReference = new ExternalModelReference();
        exportReference.setTarget("exportTarget");
        exportReference.setExternalModel(exportImportExternalModel);

        Operation operation = mock(Operation.class);
        Calculation calculation = new Calculation();
        Argument argument1 = mock(Argument.class);
        Argument argument2 = mock(Argument.class);
        calculation.setOperation(operation);
        calculation.setArguments(Arrays.asList(argument1, argument2));

        parameterModel = systemModel.getParameters().get(0);
        parameterModel.setValueReference(valueReference);
        parameterModel.setExportReference(exportReference);
        parameterModel.setCalculation(calculation);


        String name = "name";
        ParameterModel newParameterModel = parameterModelService.cloneParameterModel(name, parameterModel);

        assertNotNull(newParameterModel);
        assertNotEquals(parameterModel, newParameterModel);
        assertNotEquals(parameterModel.getUuid(), newParameterModel.getUuid());
        assertEquals(name, newParameterModel.getName());
        assertEquals(parameterModel.getValue(), newParameterModel.getValue());
        assertEquals(parameterModel.getUnit(), newParameterModel.getUnit());
        assertEquals(parameterModel.getNature(), newParameterModel.getNature());
        assertEquals(parameterModel.getValueSource(), newParameterModel.getValueSource());
        assertFalse(parameterModel.getValueReference() == newParameterModel.getValueReference());
        assertEquals(valueReference, newParameterModel.getValueReference());
        assertEquals(parameterModel.getValueReference().getTarget(), newParameterModel.getValueReference().getTarget());
        assertEquals(importExportExternalModel, newParameterModel.getValueReference().getExternalModel());
        assertEquals(parameterModel.getValueLink(), newParameterModel.getValueLink());
        assertEquals(parameterModel.getCalculation(), newParameterModel.getCalculation());
        assertEquals(parameterModel.getImportModel(), newParameterModel.getImportModel());
        assertEquals(parameterModel.getImportField(), newParameterModel.getImportField());
        assertEquals(parameterModel.getIsReferenceValueOverridden(), newParameterModel.getIsReferenceValueOverridden());
        assertEquals(parameterModel.getIsExported(), newParameterModel.getIsExported());
        assertFalse(parameterModel.getExportReference() == newParameterModel.getExportReference());
        assertEquals(exportReference, newParameterModel.getExportReference());
        assertEquals(parameterModel.getExportReference().getTarget(), newParameterModel.getExportReference().getTarget());
        assertEquals(exportImportExternalModel, newParameterModel.getExportReference().getExternalModel());
        assertEquals(parameterModel.getExportModel(), newParameterModel.getExportModel());
        assertEquals(parameterModel.getExportField(), newParameterModel.getExportField());
        assertEquals(parameterModel.getDescription(), newParameterModel.getDescription());
        assertEquals(parameterModel.getLastModification(), newParameterModel.getLastModification());
        assertFalse(parameterModel.getCalculation() == newParameterModel.getCalculation());
        assertEquals(parameterModel.getCalculation(), newParameterModel.getCalculation());
        assertEquals(parameterModel.getCalculation().getOperation(), newParameterModel.getCalculation().getOperation());
        assertEquals(operation, newParameterModel.getCalculation().getOperation());
        assertEquals(parameterModel.getCalculation().getArguments(), newParameterModel.getCalculation().getArguments());
        assertEquals(2, newParameterModel.getCalculation().getArguments().size());
        assertThat(parameterModel.getCalculation().getArguments(), hasItem(argument1));
        assertThat(parameterModel.getCalculation().getArguments(), hasItem(argument2));
        assertEquals(systemModel, newParameterModel.getParent());
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
    public void testParameterModelChangeHistory() {
        parameterModel.setName("parameter-1-renamed");

        //ParameterModel newParameterModel = new ParameterModel("new-parameter-A", 3.1415);
        //systemModel.addParameter(newParameterModel);

        systemModelRepository.saveAndFlush(systemModel);

        List<ParameterRevision> changeHistory = parameterModelService.parameterModelChangeHistory(parameterModel);

        Assert.assertEquals(2, changeHistory.size());
        Assert.assertEquals(ADMIN, changeHistory.get(0).getRevisionAuthor());
    }
}
