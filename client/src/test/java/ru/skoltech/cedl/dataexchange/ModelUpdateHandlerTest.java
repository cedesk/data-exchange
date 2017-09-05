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

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.external.ExternalModelAccessorFactory;
import ru.skoltech.cedl.dataexchange.external.ExternalModelEvaluator;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.external.ExternalModelExporter;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.structure.ModelUpdateHandler;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by Nikolay Groshkov on 05-Sep-17.
 */
public class ModelUpdateHandlerTest extends AbstractApplicationContextTest {

    private static final String NAN_VALUE_REFERENCE_TARGET = "NaN";
    private static final String ERROR_VALUE_REFERENCE_TARGET = "Error";
    private static final String VALUE_REFERENCE_TARGET = "value";

    private static final Double VALUE1 = 10d;
    private static final Double VALUE2 = 20d;

    private ExternalModelExporter externalModelExporter;
    private ParameterLinkRegistry parameterLinkRegistry;
    private ModelUpdateHandler modelUpdateHandler;

    private ExternalModel externalModel;
    private ExternalModel correctExternalModel;
    private ExternalModel wrongExternalModel;

    @Before
    public void prepare() throws ExternalModelException {
        externalModel = new ExternalModel();
        externalModel.setName("externalModel");

        correctExternalModel = new ExternalModel();
        correctExternalModel.setName("correctExternalModel");

        wrongExternalModel = new ExternalModel();
        wrongExternalModel.setName("wrongExternalModel");

        ExternalModelEvaluator externalModelEvaluator = mock(ExternalModelEvaluator.class);
        when(externalModelEvaluator.getValue(NAN_VALUE_REFERENCE_TARGET)).thenReturn(Double.NaN);
        when(externalModelEvaluator.getValue(VALUE_REFERENCE_TARGET)).thenReturn(VALUE1);

        externalModelExporter = mock(ExternalModelExporter.class);
        doThrow(ExternalModelException.class).when(externalModelExporter).setValue(eq(ERROR_VALUE_REFERENCE_TARGET), any(Double.class));

        ExternalModelAccessorFactory externalModelAccessorFactory = mock(ExternalModelAccessorFactory.class);
        when(externalModelAccessorFactory.getEvaluator(any())).thenReturn(externalModelEvaluator);
        when(externalModelAccessorFactory.getEvaluator(wrongExternalModel)).thenThrow(Exception.class);
        when(externalModelAccessorFactory.getExporter(any())).thenReturn(externalModelExporter);

        parameterLinkRegistry = mock(ParameterLinkRegistry.class);
        StatusLogger statusLogger = mock(StatusLogger.class);

        modelUpdateHandler = context.getBean(ModelUpdateHandler.class);
        modelUpdateHandler.setExternalModelAccessorFactory(externalModelAccessorFactory);
        modelUpdateHandler.setParameterLinkRegistry(parameterLinkRegistry);
        modelUpdateHandler.setStatusLogger(statusLogger);
    }

    @Test
    public void testApplyListParameterChangesFromExternalModel() {
        List<ParameterModel> parameterModels = modelUpdateHandler.applyParameterChangesFromExternalModel((ExternalModel) null);
        assertThat(parameterModels, empty());

        parameterModels = modelUpdateHandler.applyParameterChangesFromExternalModel(externalModel);
        assertThat(parameterModels, empty());

        ModelNode parentModelNode = mock(ModelNode.class);
        externalModel.setParent(parentModelNode);
        parameterModels = modelUpdateHandler.applyParameterChangesFromExternalModel(externalModel);
        assertThat(parameterModels, empty());


        ParameterModel wrongParameterModel1 = new ParameterModel();
        wrongParameterModel1.setValueSource(ParameterValueSource.REFERENCE);

        ExternalModelReference externalModelReference1 = new ExternalModelReference();
        ParameterModel wrongParameterModel2 = new ParameterModel();
        wrongParameterModel2.setValueSource(ParameterValueSource.REFERENCE);
        wrongParameterModel2.setValueReference(externalModelReference1);

        ExternalModelReference externalModelReference3 = new ExternalModelReference();
        externalModelReference3.setExternalModel(externalModel);
        ParameterModel wrongParameterModel3 = new ParameterModel();
        wrongParameterModel3.setValueSource(ParameterValueSource.MANUAL);
        wrongParameterModel3.setValueReference(externalModelReference3);

        ExternalModelReference externalModelReference4 = new ExternalModelReference();
        externalModelReference4.setExternalModel(externalModel);
        externalModelReference4.setTarget(NAN_VALUE_REFERENCE_TARGET);
        ParameterModel wrongParameterModel4 = new ParameterModel();
        wrongParameterModel4.setValueSource(ParameterValueSource.REFERENCE);
        wrongParameterModel4.setValueReference(externalModelReference4);

        ExternalModelReference externalModelReference5 = new ExternalModelReference();
        externalModelReference5.setExternalModel(wrongExternalModel);
        externalModelReference5.setTarget(VALUE_REFERENCE_TARGET);
        ParameterModel wrongParameterModel5 = new ParameterModel();
        wrongParameterModel5.setValueSource(ParameterValueSource.REFERENCE);
        wrongParameterModel5.setValueReference(externalModelReference5);

        ExternalModelReference externalModelReference6 = new ExternalModelReference();
        externalModelReference6.setExternalModel(externalModel);
        externalModelReference6.setTarget(VALUE_REFERENCE_TARGET);
        ParameterModel wrongParameterModel6 = new ParameterModel();
        wrongParameterModel6.setValueSource(ParameterValueSource.REFERENCE);
        wrongParameterModel6.setValueReference(externalModelReference6);
        wrongParameterModel6.setValue(VALUE1);

        String correctParameterModelName = "correctParameterModel";
        ExternalModelReference externalModelReference = new ExternalModelReference();
        externalModelReference.setTarget(VALUE_REFERENCE_TARGET);
        externalModelReference.setExternalModel(externalModel);
        ParameterModel correctParameterModel = new ParameterModel();
        correctParameterModel.setName(correctParameterModelName);
        correctParameterModel.setValueSource(ParameterValueSource.REFERENCE);
        correctParameterModel.setValueReference(externalModelReference);
        correctParameterModel.setValue(VALUE2);

        List<ParameterModel> parentParameterModels
                = Arrays.asList(wrongParameterModel1, wrongParameterModel2, wrongParameterModel3,
                wrongParameterModel4, wrongParameterModel5, wrongParameterModel6, correctParameterModel);
        when(parentModelNode.getParameters()).thenReturn(parentParameterModels);
        parameterModels = modelUpdateHandler.applyParameterChangesFromExternalModel(externalModel);
        assertThat(parameterModels, hasSize(1));
        assertEquals(parameterModels.get(0).getName(), correctParameterModelName);
        assertEquals(parameterModels.get(0).getValue(), VALUE1);
        verify(parameterLinkRegistry, atLeastOnce()).updateSinks(correctParameterModel);
    }

    @Test
    public void testApplyParameterChangesFromExternalModel() {
        assertNull(modelUpdateHandler.applyParameterChangesFromExternalModel((ParameterModel) null));

        ParameterModel parameterModel = new ParameterModel();
        parameterModel.setValueSource(ParameterValueSource.REFERENCE);
        assertNull(modelUpdateHandler.applyParameterChangesFromExternalModel(parameterModel));

        ExternalModelReference externalModelReference = new ExternalModelReference();
        parameterModel.setValueReference(externalModelReference);
        assertNull(modelUpdateHandler.applyParameterChangesFromExternalModel(parameterModel));

        externalModelReference.setExternalModel(wrongExternalModel);
        parameterModel.setValueSource(ParameterValueSource.MANUAL);
        assertNull(modelUpdateHandler.applyParameterChangesFromExternalModel(parameterModel));

        parameterModel.setValueSource(ParameterValueSource.REFERENCE);
        assertNull(modelUpdateHandler.applyParameterChangesFromExternalModel(parameterModel));

        externalModelReference.setExternalModel(correctExternalModel);
        externalModelReference.setTarget(NAN_VALUE_REFERENCE_TARGET);
        assertNull(modelUpdateHandler.applyParameterChangesFromExternalModel(parameterModel));

        externalModelReference.setExternalModel(correctExternalModel);
        externalModelReference.setTarget(VALUE_REFERENCE_TARGET);
        parameterModel.setValue(VALUE1);
        assertNull(modelUpdateHandler.applyParameterChangesFromExternalModel(parameterModel));

        externalModelReference.setExternalModel(correctExternalModel);
        externalModelReference.setTarget(VALUE_REFERENCE_TARGET);
        parameterModel.setValue(VALUE2);
        ParameterModel updatedParameterModel = modelUpdateHandler.applyParameterChangesFromExternalModel(parameterModel);
        assertNotNull(updatedParameterModel);
        assertEquals(VALUE1, updatedParameterModel.getValue());
        verify(parameterLinkRegistry, atLeastOnce()).updateSinks(parameterModel);
    }

    @Test
    public void testApplyParameterChangesToExternalModel() throws ExternalModelException {
        ParameterModel parameterModel1 = new ParameterModel();

        ModelNode modelNode = mock(ModelNode.class);
        when(modelNode.getParameters()).thenReturn(Collections.singletonList(parameterModel1));

        ExternalModel externalModel = new ExternalModel();
        externalModel.setParent(modelNode);

        parameterModel1.setIsExported(false);
        List<ParameterModel> parameterModels = modelUpdateHandler.applyParameterChangesToExternalModel(externalModel);
        assertThat(parameterModels, empty());
        verify(externalModelExporter, times(NumberUtils.INTEGER_ZERO)).setValue(any(String.class), any(Double.class));

        parameterModel1.setIsExported(true);
        parameterModel1.setExportReference(null);
        parameterModels = modelUpdateHandler.applyParameterChangesToExternalModel(externalModel);
        assertThat(parameterModels, empty());
        verify(externalModelExporter, times(NumberUtils.INTEGER_ZERO)).setValue(any(String.class), any(Double.class));

        ExternalModelReference externalModelReference = new ExternalModelReference();
        externalModelReference.setExternalModel(null);
        parameterModel1.setExportReference(externalModelReference);
        parameterModels = modelUpdateHandler.applyParameterChangesToExternalModel(externalModel);
        assertThat(parameterModels, empty());
        verify(externalModelExporter, times(NumberUtils.INTEGER_ZERO)).setValue(any(String.class), any(Double.class));

        externalModelReference.setExternalModel(externalModel);
        externalModelReference.setTarget(null);
        parameterModels = modelUpdateHandler.applyParameterChangesToExternalModel(externalModel);
        assertThat(parameterModels, empty());
        verify(externalModelExporter, times(NumberUtils.INTEGER_ZERO)).setValue(any(String.class), any(Double.class));

        externalModelReference.setTarget("");
        parameterModels = modelUpdateHandler.applyParameterChangesToExternalModel(externalModel);
        assertThat(parameterModels, empty());
        verify(externalModelExporter, times(NumberUtils.INTEGER_ZERO)).setValue(any(String.class), any(Double.class));

        externalModelReference.setTarget(ERROR_VALUE_REFERENCE_TARGET);
        parameterModels = modelUpdateHandler.applyParameterChangesToExternalModel(externalModel);
        assertThat(parameterModels, empty());
        verify(externalModelExporter, atLeastOnce()).setValue(any(String.class), any(Double.class));
        verify(externalModelExporter, atLeastOnce()).flushModifications();

        externalModelReference.setTarget(VALUE_REFERENCE_TARGET);
        parameterModels = modelUpdateHandler.applyParameterChangesToExternalModel(externalModel);
        assertThat(parameterModels, hasSize(1));
        verify(externalModelExporter, atLeastOnce()).setValue(any(String.class), any(Double.class));
        verify(externalModelExporter, atLeastOnce()).flushModifications();

        ParameterModel parameterModel2 = new ParameterModel();
        when(modelNode.getParameters()).thenReturn(Arrays.asList(parameterModel1, parameterModel2));
        parameterModels = modelUpdateHandler.applyParameterChangesToExternalModel(externalModel);
        assertThat(parameterModels, hasSize(1));
        verify(externalModelExporter, atLeastOnce()).setValue(any(String.class), any(Double.class));
        verify(externalModelExporter, atLeastOnce()).flushModifications();
    }

}
