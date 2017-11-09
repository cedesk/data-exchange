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
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.structure.update.ParameterModelUpdateState;
import ru.skoltech.cedl.dataexchange.structure.update.ParameterReferenceValidity;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by D.Knoll on 27.03.2015.
 */
public class ParameterModelTest {

    @Test
    public void testEffectiveValue() {
        ParameterModel parameterModel = new ParameterModel("parameterModel", 10.1);
        assertEquals(10.1, parameterModel.getEffectiveValue(), 0);

        parameterModel.setIsReferenceValueOverridden(true);
        assertEquals(Double.NaN, parameterModel.getEffectiveValue(), 0);

        parameterModel.setOverrideValue(20.1);
        assertEquals(20.1, parameterModel.getEffectiveValue(), 0);

        parameterModel.setValueSource(ParameterValueSource.LINK);
        assertEquals(20.1, parameterModel.getEffectiveValue(), 0);

        parameterModel.setValueSource(ParameterValueSource.CALCULATION);
        assertEquals(20.1, parameterModel.getEffectiveValue(), 0);

        parameterModel.setIsReferenceValueOverridden(false);
        parameterModel.setValueSource(ParameterValueSource.LINK);
        ParameterModel valueLink = mock(ParameterModel.class);
        doReturn(30.1).when(valueLink).getEffectiveValue();
        parameterModel.setValueLink(valueLink);
        assertEquals(30.1, parameterModel.getEffectiveValue(), 0);

        parameterModel.setIsReferenceValueOverridden(false);
        parameterModel.setValueSource(ParameterValueSource.CALCULATION);
        Calculation calculation = mock(Calculation.class);
        doReturn(true).when(calculation).valid();
        doReturn(40.1).when(calculation).evaluate();
        parameterModel.setCalculation(calculation);
        assertEquals(40.1, parameterModel.getEffectiveValue(), 0);
    }

    @Test
    public void testExportAndImport() {
        String exportField = "exportField";
        String importField = "importField";
        ParameterModel parameterModel = new ParameterModel();
        assertNull(parameterModel.getExportField());
        assertNull(parameterModel.getExportModel());
        assertNull(parameterModel.getImportField());
        assertNull(parameterModel.getImportModel());

        parameterModel.setExportField(exportField);
        assertEquals(exportField, parameterModel.getExportField());

        ExternalModel exportModel = mock(ExternalModel.class);
        parameterModel.setExportModel(exportModel);
        assertEquals(exportModel, parameterModel.getExportModel());

        parameterModel.setImportField(importField);
        assertEquals(importField, parameterModel.getImportField());

        ExternalModel importModel = mock(ExternalModel.class);
        parameterModel.setImportModel(importModel);
        assertEquals(importModel, parameterModel.getImportModel());
    }

    @Test
    public void testValidateValueReference() {
        ParameterModel parameterModel = new ParameterModel();
        assertNull(parameterModel.validateValueReference());
        assertFalse(parameterModel.isValidValueReference());

        parameterModel.setValueSource(ParameterValueSource.REFERENCE);
        assertEquals(ParameterReferenceValidity.INVALID_EMPTY_REFERENCE, parameterModel.validateValueReference());
        assertFalse(parameterModel.isValidValueReference());

        ExternalModelReference externalModelReference = new ExternalModelReference();
        parameterModel.setValueReference(externalModelReference);
        assertEquals(ParameterReferenceValidity.INVALID_EMPTY_REFERENCE_EXTERNAL_MODEL, parameterModel.validateValueReference());
        assertFalse(parameterModel.isValidValueReference());

        ExternalModel externalModel = mock(ExternalModel.class);
        externalModelReference.setExternalModel(externalModel);
        assertEquals(ParameterReferenceValidity.INVALID_EMPTY_REFERENCE_TARGET, parameterModel.validateValueReference());
        assertFalse(parameterModel.isValidValueReference());

        externalModelReference.setTarget("");
        assertEquals(ParameterReferenceValidity.INVALID_EMPTY_REFERENCE_TARGET, parameterModel.validateValueReference());
        assertFalse(parameterModel.isValidValueReference());

        externalModelReference.setTarget("target");
        assertEquals(ParameterReferenceValidity.VALID, parameterModel.validateValueReference());
        assertTrue(parameterModel.isValidValueReference());
    }

    @Test
    public void testValidateExportReference() {
        ParameterModel parameterModel = new ParameterModel();
        assertNull(parameterModel.validateExportReference());
        assertFalse(parameterModel.isValidExportReference());

        parameterModel.setIsExported(true);
        assertEquals(ParameterReferenceValidity.INVALID_EMPTY_REFERENCE, parameterModel.validateExportReference());
        assertFalse(parameterModel.isValidExportReference());

        ExternalModelReference externalModelReference = new ExternalModelReference();
        parameterModel.setExportReference(externalModelReference);
        assertEquals(ParameterReferenceValidity.INVALID_EMPTY_REFERENCE_EXTERNAL_MODEL, parameterModel.validateExportReference());
        assertFalse(parameterModel.isValidExportReference());

        ExternalModel externalModel = mock(ExternalModel.class);
        externalModelReference.setExternalModel(externalModel);
        assertEquals(ParameterReferenceValidity.INVALID_EMPTY_REFERENCE_TARGET, parameterModel.validateExportReference());
        assertFalse(parameterModel.isValidExportReference());

        externalModelReference.setTarget("");
        assertEquals(ParameterReferenceValidity.INVALID_EMPTY_REFERENCE_TARGET, parameterModel.validateExportReference());
        assertFalse(parameterModel.isValidExportReference());

        externalModelReference.setTarget("target");
        assertEquals(ParameterReferenceValidity.VALID, parameterModel.validateExportReference());
        assertTrue(parameterModel.isValidExportReference());
    }

    @Test
    public void testUpdateValueReference() throws ExternalModelException {
        ParameterModel parameterModel = new ParameterModel();
        boolean update = parameterModel.updateValueReference();
        assertFalse(update);
        assertNull(parameterModel.getLastValueReferenceUpdateState());

        String target = "target";
        ExternalModel externalModel = mock(ExternalModel.class);

        ExternalModelReference valueReference = new ExternalModelReference();
        valueReference.setExternalModel(externalModel);
        valueReference.setTarget(target);
        parameterModel.setValueSource(ParameterValueSource.REFERENCE);
        parameterModel.setValueReference(valueReference);

        doThrow(ExternalModelException.class).when(externalModel).getValue(target);
        update = parameterModel.updateValueReference();
        assertFalse(update);
        assertEquals(ParameterModelUpdateState.FAIL_EVALUATION, parameterModel.getLastValueReferenceUpdateState());

        doReturn(Double.NaN).when(externalModel).getValue(target);
        update = parameterModel.updateValueReference();
        assertFalse(update);
        assertEquals(ParameterModelUpdateState.FAIL_INVALID_VALUE, parameterModel.getLastValueReferenceUpdateState());

        parameterModel.setValue(10.001);
        doReturn(10.00100000000000000000001).when(externalModel).getValue(target);
        update = parameterModel.updateValueReference();
        assertFalse(update);
        assertEquals(ParameterModelUpdateState.SUCCESS_WITHOUT_UPDATE, parameterModel.getLastValueReferenceUpdateState());

        doReturn(20.001).when(externalModel).getValue(target);
        update = parameterModel.updateValueReference();
        assertTrue(update);
        assertEquals(ParameterModelUpdateState.SUCCESS, parameterModel.getLastValueReferenceUpdateState());
    }

    @Test
    public void testCompare() {
        ParameterModel parameterModel1 = new ParameterModel("name", 10.1);
        ParameterModel parameterModel2 = new ParameterModel("name", 10.1);
        ParameterModel parameterModel3 = new ParameterModel("anotherName", 10.1);
        ParameterModel parameterModel4 = new ParameterModel("thirdName", 10.1);

        assertEquals(0, parameterModel1.compareTo(parameterModel2));
        assertTrue(0 < parameterModel1.compareTo(parameterModel3));
        assertTrue(0 > parameterModel1.compareTo(parameterModel4));
    }

    @Test
    public void testEquals1() throws Exception {
        ParameterModel pa = new ParameterModel("power consumption", 40.10);
        ParameterModel pb = new ParameterModel("power consumption", 40.10);
        assertNotEquals("same name and value, but must be different in UUIDs", pa, pb);

        ParameterModel p1 = new ParameterModel("power consumption", 40.10);
        ParameterModel p2 = Utils.copyBean(p1, new ParameterModel());
        Assert.assertEquals("same name and value, but not equal", p1, p2);

        ParameterModel p3 = Utils.copyBean(p1, new ParameterModel());
        p3.setValue(p1.getValue() + 3.1);
        assertNotEquals("same name and different value, but equal", p1, p3);

        ParameterModel p4 = Utils.copyBean(p1, new ParameterModel());
        p4.setName(p1.getName().toUpperCase());
        assertNotEquals("different name and same value, but equal", p1, p4);
    }

    @Test
    public void testEquals2() throws Exception {

        ParameterModel p5 = new ParameterModel("power CONSUMPTION", 40.10, ParameterValueSource.MANUAL, true, "desc");
        ParameterModel p6 = new ParameterModel("power CONSUMPTION", 40.10, ParameterValueSource.REFERENCE, true, "desc");
        assertNotEquals("different type, but equal", p5, p6);

        ParameterModel p7 = new ParameterModel("power CONSUMPTION", 40.10, ParameterValueSource.REFERENCE, true, "desc");
        ParameterModel p8 = new ParameterModel("power CONSUMPTION", 40.10, ParameterValueSource.REFERENCE, false, "desc");
        assertNotEquals("different shared, but equal", p7, p8);
    }
}