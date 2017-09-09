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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.external.ExternalModelAccessor;
import ru.skoltech.cedl.dataexchange.external.ExternalModelAccessorFactory;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;
import ru.skoltech.cedl.dataexchange.structure.update.ExternalModelUpdateState;
import ru.skoltech.cedl.dataexchange.structure.update.ModelUpdateHandler;
import ru.skoltech.cedl.dataexchange.structure.update.ParameterModelUpdateState;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
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

    private ExternalModelAccessor externalModelAccessor;
    private ParameterLinkRegistry parameterLinkRegistry;
    private ModelUpdateHandler modelUpdateHandler;

    private ExternalModel externalModel;
    private ExternalModel otherExternalModel;

    @Before
    public void prepare() throws ExternalModelException {
        externalModel = new ExternalModel();
        externalModel.setName("externalModel");

        otherExternalModel = new ExternalModel();
        otherExternalModel.setName("otherExternalModel");

        externalModelAccessor = mock(ExternalModelAccessor.class);
        doReturn(Double.NaN).when(externalModelAccessor).getValue(NAN_VALUE_REFERENCE_TARGET);
        doReturn(VALUE1).when(externalModelAccessor).getValue(VALUE_REFERENCE_TARGET);
        doThrow(ExternalModelException.class).when(externalModelAccessor).getValue(eq(ERROR_VALUE_REFERENCE_TARGET));
        doThrow(ExternalModelException.class).when(externalModelAccessor).setValue(eq(ERROR_VALUE_REFERENCE_TARGET), any(Double.class));

        ExternalModelAccessorFactory externalModelAccessorFactory = mock(ExternalModelAccessorFactory.class);
        when(externalModelAccessorFactory.createAccessor(any())).thenReturn(externalModelAccessor);

        parameterLinkRegistry = mock(ParameterLinkRegistry.class);

        modelUpdateHandler = context.getBean(ModelUpdateHandler.class);
        modelUpdateHandler.setExternalModelAccessorFactory(externalModelAccessorFactory);
        modelUpdateHandler.setParameterLinkRegistry(parameterLinkRegistry);
    }

    @Test
    public void testApplyListParameterUpdatesFromExternalModel() throws IOException {
        List<Pair<ParameterModel, ParameterModelUpdateState>> updates = modelUpdateHandler.applyParameterUpdatesFromExternalModel(null);
        assertThat(updates, empty());

        updates = modelUpdateHandler.applyParameterUpdatesFromExternalModel(externalModel);
        assertThat(updates, empty());

        ModelNode parentModelNode = mock(ModelNode.class);
        externalModel.setParent(parentModelNode);
        updates = modelUpdateHandler.applyParameterUpdatesFromExternalModel(externalModel);
        assertThat(updates, empty());


        ParameterModel wrongParameterModel1 = new ParameterModel();
        wrongParameterModel1.setValueSource(ParameterValueSource.REFERENCE);

        ExternalModelReference externalModelReference2 = new ExternalModelReference();
        ParameterModel wrongParameterModel2 = new ParameterModel();
        wrongParameterModel2.setValueSource(ParameterValueSource.REFERENCE);
        wrongParameterModel2.setValueReference(externalModelReference2);

        ExternalModelReference externalModelReference3 = new ExternalModelReference();
        externalModelReference3.setExternalModel(externalModel);
        ParameterModel wrongParameterModel3 = new ParameterModel();
        wrongParameterModel3.setValueSource(ParameterValueSource.MANUAL);
        wrongParameterModel3.setValueReference(externalModelReference3);

        ExternalModelReference externalModelReference4 = new ExternalModelReference();
        externalModelReference4.setExternalModel(externalModel);
        externalModelReference4.setTarget(null);
        ParameterModel wrongParameterModel4 = new ParameterModel();
        wrongParameterModel4.setValueSource(ParameterValueSource.REFERENCE);
        wrongParameterModel4.setValueReference(externalModelReference4);

        ExternalModelReference externalModelReference5 = new ExternalModelReference();
        externalModelReference5.setExternalModel(externalModel);
        externalModelReference5.setTarget("");
        ParameterModel wrongParameterModel5 = new ParameterModel();
        wrongParameterModel5.setValueSource(ParameterValueSource.REFERENCE);
        wrongParameterModel5.setValueReference(externalModelReference5);

        ExternalModelReference externalModelReference6 = new ExternalModelReference();
        externalModelReference6.setExternalModel(externalModel);
        externalModelReference6.setTarget(NAN_VALUE_REFERENCE_TARGET);
        ParameterModel wrongParameterModel6 = new ParameterModel();
        wrongParameterModel6.setValueSource(ParameterValueSource.REFERENCE);
        wrongParameterModel6.setValueReference(externalModelReference6);

        ExternalModelReference externalModelReference7 = new ExternalModelReference();
        externalModelReference7.setExternalModel(externalModel);
        externalModelReference7.setTarget(ERROR_VALUE_REFERENCE_TARGET);
        ParameterModel wrongParameterModel7 = new ParameterModel();
        wrongParameterModel7.setValueSource(ParameterValueSource.REFERENCE);
        wrongParameterModel7.setValueReference(externalModelReference7);

        ExternalModelReference externalModelReference = new ExternalModelReference();
        externalModelReference.setExternalModel(otherExternalModel);
        externalModelReference.setTarget(VALUE_REFERENCE_TARGET);
        ParameterModel parameterModel = new ParameterModel();
        parameterModel.setValueSource(ParameterValueSource.REFERENCE);
        parameterModel.setValueReference(externalModelReference);
        parameterModel.setValue(VALUE1);

        ExternalModelReference externalModelReferenceC1 = new ExternalModelReference();
        externalModelReferenceC1.setExternalModel(externalModel);
        externalModelReferenceC1.setTarget(VALUE_REFERENCE_TARGET);
        ParameterModel correctParameterModel1 = new ParameterModel();
        correctParameterModel1.setValueSource(ParameterValueSource.REFERENCE);
        correctParameterModel1.setValueReference(externalModelReferenceC1);
        correctParameterModel1.setValue(VALUE1);

        String correctParameterModelName = "correctParameterModel";
        ExternalModelReference externalModelReferenceC2 = new ExternalModelReference();
        externalModelReferenceC2.setTarget(VALUE_REFERENCE_TARGET);
        externalModelReferenceC2.setExternalModel(externalModel);
        ParameterModel correctParameterModel2 = new ParameterModel();
        correctParameterModel2.setName(correctParameterModelName);
        correctParameterModel2.setValueSource(ParameterValueSource.REFERENCE);
        correctParameterModel2.setValueReference(externalModelReferenceC2);
        correctParameterModel2.setValue(VALUE2);

        List<ParameterModel> parentParameterModels
                = Arrays.asList(wrongParameterModel1, wrongParameterModel2, wrongParameterModel3,
                wrongParameterModel4, wrongParameterModel5, wrongParameterModel6, wrongParameterModel7,
                parameterModel, correctParameterModel1, correctParameterModel2);
        when(parentModelNode.getParameters()).thenReturn(parentParameterModels);
        updates = modelUpdateHandler.applyParameterUpdatesFromExternalModel(externalModel);
        assertThat(updates, hasSize(8));
        assertThat(updates, hasItem(Pair.of(wrongParameterModel1, ParameterModelUpdateState.FAIL_EMPTY_REFERENCE)));
        assertThat(updates, hasItem(Pair.of(wrongParameterModel2, ParameterModelUpdateState.FAIL_EMPTY_REFERENCE_EXTERNAL_MODEL)));
        assertThat(updates, hasItem(Pair.of(wrongParameterModel4, ParameterModelUpdateState.FAIL_EMPTY_REFERENCE_TARGET)));
        assertThat(updates, hasItem(Pair.of(wrongParameterModel5, ParameterModelUpdateState.FAIL_EMPTY_REFERENCE_TARGET)));
        assertThat(updates, hasItem(Pair.of(wrongParameterModel6, ParameterModelUpdateState.FAIL_INVALID_VALUE)));
        assertThat(updates, hasItem(Pair.of(wrongParameterModel7, ParameterModelUpdateState.FAIL_EVALUATION)));
        assertThat(updates, hasItem(Pair.of(correctParameterModel1, ParameterModelUpdateState.SUCCESS)));
        assertThat(updates, hasItem(Pair.of(correctParameterModel2, ParameterModelUpdateState.SUCCESS)));
        verify(parameterLinkRegistry, only()).updateSinks(any());
        verify(parameterLinkRegistry, only()).updateSinks(correctParameterModel2);

        doThrow(IOException.class).when(externalModelAccessor).close();
        when(parentModelNode.getParameters()).thenReturn(Collections.singletonList(correctParameterModel1));
        updates = modelUpdateHandler.applyParameterUpdatesFromExternalModel(externalModel);
        assertThat(updates, hasSize(1));
        assertThat(updates, hasItem(Pair.of(correctParameterModel1, ParameterModelUpdateState.SUCCESS)));
    }

    @Test
    public void testApplyParameterUpdateFromExternalModel() {
        assertNull(modelUpdateHandler.applyParameterUpdateFromExternalModel(null));

        ParameterModel parameterModel = new ParameterModel();
        parameterModel.setValueSource(ParameterValueSource.REFERENCE);
        Pair<ParameterModel, ParameterModelUpdateState> update = modelUpdateHandler.applyParameterUpdateFromExternalModel(parameterModel);
        assertEquals(parameterModel, update.getLeft());
        assertEquals(ParameterModelUpdateState.FAIL_EMPTY_REFERENCE, update.getRight());

        ExternalModelReference externalModelReference = new ExternalModelReference();
        parameterModel.setValueReference(externalModelReference);
        update = modelUpdateHandler.applyParameterUpdateFromExternalModel(parameterModel);
        assertEquals(parameterModel, update.getLeft());
        assertEquals(ParameterModelUpdateState.FAIL_EMPTY_REFERENCE_EXTERNAL_MODEL, update.getRight());

        externalModelReference.setExternalModel(externalModel);
        parameterModel.setValueSource(ParameterValueSource.MANUAL);
        assertNull(modelUpdateHandler.applyParameterUpdateFromExternalModel(parameterModel));

        parameterModel.setValueSource(ParameterValueSource.REFERENCE);
        update = modelUpdateHandler.applyParameterUpdateFromExternalModel(parameterModel);
        assertEquals(parameterModel, update.getLeft());
        assertEquals(ParameterModelUpdateState.FAIL_EMPTY_REFERENCE_TARGET, update.getRight());

        parameterModel.setValueSource(ParameterValueSource.REFERENCE);
        externalModelReference.setTarget(ERROR_VALUE_REFERENCE_TARGET);
        update = modelUpdateHandler.applyParameterUpdateFromExternalModel(parameterModel);
        assertEquals(parameterModel, update.getLeft());
        assertEquals(ParameterModelUpdateState.FAIL_EVALUATION, update.getRight());

        externalModelReference.setTarget(NAN_VALUE_REFERENCE_TARGET);
        update = modelUpdateHandler.applyParameterUpdateFromExternalModel(parameterModel);
        assertEquals(parameterModel, update.getLeft());
        assertEquals(ParameterModelUpdateState.FAIL_INVALID_VALUE, update.getRight());

        externalModelReference.setExternalModel(externalModel);
        externalModelReference.setTarget(VALUE_REFERENCE_TARGET);
        parameterModel.setValue(VALUE1);
        update = modelUpdateHandler.applyParameterUpdateFromExternalModel(parameterModel);
        assertEquals(parameterModel, update.getLeft());
        assertEquals(ParameterModelUpdateState.SUCCESS, update.getRight());
        verify(parameterLinkRegistry, never()).updateSinks(parameterModel);

        externalModelReference.setExternalModel(externalModel);
        externalModelReference.setTarget(VALUE_REFERENCE_TARGET);
        parameterModel.setValue(VALUE2);
        update = modelUpdateHandler.applyParameterUpdateFromExternalModel(parameterModel);
        assertEquals(parameterModel, update.getLeft());
        assertEquals(ParameterModelUpdateState.SUCCESS, update.getRight());
        assertEquals(VALUE1, update.getLeft().getValue());
        assertEquals(ParameterModelUpdateState.SUCCESS, update.getRight());
        verify(parameterLinkRegistry, only()).updateSinks(any());
        verify(parameterLinkRegistry, only()).updateSinks(parameterModel);
    }

    @Test
    public void testApplyParameterUpdatesToExternalModel() throws ExternalModelException, IOException {
        ParameterModel parameterModel1 = new ParameterModel();

        ModelNode modelNode = mock(ModelNode.class);
        when(modelNode.getParameters()).thenReturn(Collections.singletonList(parameterModel1));

        ExternalModel externalModel = new ExternalModel();
        externalModel.setParent(modelNode);

        parameterModel1.setIsExported(false);
        List<Pair<ParameterModel, ExternalModelUpdateState>> updates = modelUpdateHandler.applyParameterUpdatesToExternalModel(externalModel);
        assertThat(updates, empty());
        verify(externalModelAccessor, never()).setValue(any(String.class), any(Double.class));

        parameterModel1.setIsExported(true);
        parameterModel1.setValueSource(ParameterValueSource.MANUAL);
        updates = modelUpdateHandler.applyParameterUpdatesToExternalModel(externalModel);
        assertThat(updates, empty());
        verify(externalModelAccessor, never()).setValue(any(String.class), any(Double.class));

        parameterModel1.setValueSource(ParameterValueSource.REFERENCE);
        updates = modelUpdateHandler.applyParameterUpdatesToExternalModel(externalModel);
        assertThat(updates, hasSize(1));
        assertThat(updates, hasItem(Pair.of(parameterModel1, ExternalModelUpdateState.FAIL_EMPTY_REFERENCE)));
        verify(externalModelAccessor, never()).setValue(any(String.class), any(Double.class));

        ExternalModelReference externalModelReference = new ExternalModelReference();
        externalModelReference.setExternalModel(null);
        parameterModel1.setExportReference(externalModelReference);
        updates = modelUpdateHandler.applyParameterUpdatesToExternalModel(externalModel);
        assertThat(updates, hasSize(1));
        assertThat(updates, hasItem(Pair.of(parameterModel1, ExternalModelUpdateState.FAIL_EMPTY_REFERENCE_EXTERNAL_MODEL)));
        verify(externalModelAccessor, never()).setValue(any(String.class), any(Double.class));

        externalModelReference.setExternalModel(externalModel);
        externalModelReference.setTarget(null);
        updates = modelUpdateHandler.applyParameterUpdatesToExternalModel(externalModel);
        assertThat(updates, hasSize(1));
        assertThat(updates, hasItem(Pair.of(parameterModel1, ExternalModelUpdateState.FAIL_EMPTY_REFERENCE_TARGET)));
        verify(externalModelAccessor, never()).setValue(any(String.class), any(Double.class));

        externalModelReference.setTarget("");
        updates = modelUpdateHandler.applyParameterUpdatesToExternalModel(externalModel);
        assertThat(updates, hasSize(1));
        assertThat(updates, hasItem(Pair.of(parameterModel1, ExternalModelUpdateState.FAIL_EMPTY_REFERENCE_TARGET)));
        verify(externalModelAccessor, never()).setValue(any(String.class), any(Double.class));

        externalModelReference.setTarget(ERROR_VALUE_REFERENCE_TARGET);
        updates = modelUpdateHandler.applyParameterUpdatesToExternalModel(externalModel);
        assertThat(updates, hasSize(1));
        assertThat(updates, hasItem(Pair.of(parameterModel1, ExternalModelUpdateState.FAIL_EXPORT)));
        verify(externalModelAccessor, times(1)).setValue(any(String.class), any(Double.class));
        verify(externalModelAccessor, never()).flush();

        externalModelReference.setTarget(VALUE_REFERENCE_TARGET);
        updates = modelUpdateHandler.applyParameterUpdatesToExternalModel(externalModel);
        assertThat(updates, hasSize(1));
        assertThat(updates, hasItem(Pair.of(parameterModel1, ExternalModelUpdateState.SUCCESS)));
        verify(externalModelAccessor, times(2)).setValue(any(String.class), any(Double.class));
        verify(externalModelAccessor, times(1)).flush();

        ParameterModel parameterModel2 = new ParameterModel();
        when(modelNode.getParameters()).thenReturn(Arrays.asList(parameterModel1, parameterModel2));
        updates = modelUpdateHandler.applyParameterUpdatesToExternalModel(externalModel);
        assertThat(updates, hasSize(1));
        assertThat(updates, hasItem(Pair.of(parameterModel1, ExternalModelUpdateState.SUCCESS)));
        verify(externalModelAccessor, times(3)).setValue(any(String.class), any(Double.class));
        verify(externalModelAccessor, times(2)).flush();
    }

    @Test(expected = ExternalModelException.class)
    public void testErrorApplyParameterUpdatesToExternalModel() throws ExternalModelException, IOException {
        ModelNode modelNode = mock(ModelNode.class);

        ExternalModel externalModel = new ExternalModel();
        externalModel.setParent(modelNode);

        ExternalModelReference externalModelReference = new ExternalModelReference();
        externalModelReference.setExternalModel(externalModel);
        externalModelReference.setTarget(VALUE_REFERENCE_TARGET);

        ParameterModel parameterModel = new ParameterModel();
        parameterModel.setIsExported(true);
        parameterModel.setValueSource(ParameterValueSource.REFERENCE);
        parameterModel.setExportReference(externalModelReference);

        when(modelNode.getParameters()).thenReturn(Collections.singletonList(parameterModel));
        doThrow(IOException.class).when(externalModelAccessor).flush();
        modelUpdateHandler.applyParameterUpdatesToExternalModel(externalModel);
    }
}
