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

import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.revision.ExternalModelRepository;
import ru.skoltech.cedl.dataexchange.repository.revision.SystemModelRepository;
import ru.skoltech.cedl.dataexchange.service.ExternalModelFileStorageService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * Created by D.Knoll on 02.07.2015.
 */
public class ExternalModelTest extends AbstractApplicationContextTest {

    private SystemModelRepository systemModelRepository;
    private ExternalModelRepository externalModelRepository;
    private ExternalModelFileStorageService externalModelFileStorageService;

    @Before
    public void prepare() {
        systemModelRepository = context.getBean(SystemModelRepository.class);
        externalModelRepository = context.getBean(ExternalModelRepository.class);
        externalModelFileStorageService = context.getBean(ExternalModelFileStorageService.class);

        File file = new File("target/foo.test");
        if (file.exists()) {
            boolean deleted = file.delete();
            assertTrue(deleted);
        }
    }

    @Test()
    public void storeAndRetrieveAttachment() throws URISyntaxException, IOException {
        File file = new File(this.getClass().getResource("/attachment.xls").toURI());

        SystemModel testSat = new SystemModel("testSat");
        systemModelRepository.saveAndFlush(testSat);

        ExternalModel externalModel = externalModelFileStorageService.createExternalModelFromFile(file, testSat);

        assertEquals(0, externalModel.getId());
        ExternalModel externalModel1 = externalModelRepository.saveAndFlush(externalModel);
        long pk = externalModel.getId();
        assertNotEquals(0, pk);

        ExternalModel externalModel2 = externalModelRepository.findOne(pk);

        assertArrayEquals(externalModel1.getAttachment(), externalModel2.getAttachment());
    }

    @Test
    public void testExternalModelReferences() throws URISyntaxException, IOException {
        File file = new File(this.getClass().getResource("/attachment.xls").toURI());

        SystemModel testSat = new SystemModel("testSat");
        systemModelRepository.saveAndFlush(testSat);

        ExternalModel externalModel = externalModelFileStorageService.createExternalModelFromFile(file, testSat);
        ExternalModelReference externalModelReference = new ExternalModelReference();
        externalModelReference.setExternalModel(externalModel);
        externalModelReference.setTarget("AA11");

        ParameterModel parameterModel = new ParameterModel("testPar", 592.65);
        parameterModel.setValueSource(ParameterValueSource.REFERENCE);
        parameterModel.setValueReference(externalModelReference);

        testSat.addParameter(parameterModel);
        SystemModel systemModel = systemModelRepository.saveAndFlush(testSat);

        ExternalModelReference valueReference = systemModel.getParameters().get(0).getValueReference();
        assertEquals(externalModelReference, valueReference);

        ExternalModel newExternalModel = externalModelFileStorageService.createExternalModelFromFile(file, testSat);
        valueReference.setExternalModel(newExternalModel);

        systemModelRepository.saveAndFlush(systemModel);

        SystemModel savedSystemModel = systemModelRepository.findOne(testSat.getId());

        ExternalModelReference savedValueReference = savedSystemModel.getParameters().get(0).getValueReference();
        assertEquals(valueReference, savedValueReference);
        ExternalModelReference savedExportReference = savedSystemModel.getParameters().get(0).getExportReference();
        assertNotEquals(savedValueReference, savedExportReference);
    }

    @Test
    public void testExternalModelReferencesChange() throws URISyntaxException, IOException {
        File file = new File(this.getClass().getResource("/attachment.xls").toURI());

        SystemModel testSat = new SystemModel("testSat");
        systemModelRepository.saveAndFlush(testSat);

        ExternalModel externalModel = externalModelFileStorageService.createExternalModelFromFile(file, testSat);
        ExternalModelReference externalModelReference = new ExternalModelReference();
        externalModelReference.setExternalModel(externalModel);
        externalModelReference.setTarget("AA11");

        Double value = 592.65;
        ParameterModel parameterModel = new ParameterModel("testPar", value);
        parameterModel.setValueSource(ParameterValueSource.REFERENCE);
        parameterModel.setValueReference(externalModelReference);

        testSat.addParameter(parameterModel);
        SystemModel systemModel = systemModelRepository.saveAndFlush(testSat);

        assertNotNull(systemModel);
        parameterModel = systemModel.getParameters().get(0);
        assertNotNull(parameterModel);
        assertEquals(ParameterValueSource.REFERENCE, parameterModel.getValueSource());
        assertEquals(value, Double.valueOf(parameterModel.getEffectiveValue()));

        ExternalModelReference valueReference = systemModel.getParameters().get(0).getValueReference();
        assertEquals(externalModelReference, valueReference);

//        project.addExternalModelChangeObserver((o, arg) -> {
//            ExternalModel externalModel = (ExternalModel) arg;
//            try {
//                modelUpdateHandler.applyParameterUpdatesFromExternalModel(externalModel,
//                        Arrays.asList(new ExternalModelUpdateListener(), new ExternalModelLogListener()), new ModelEditingController.ParameterUpdateListener());
//            } catch (ExternalModelException e) {
//                logger.error("error updating parameters from external model '" + externalModel.getNodePath() + "'");
//            }
//        });

        System.out.println(parameterModel.getEffectiveValue());

//        modelUpdateService.applyParameterUpdatesFromExternalModel(project, externalModel,
//                parameterLinkRegistry, externalModelFileHandler,
//                Collections.singletonList(externalModelUpdateListener), parameterUpdateListener);


    }



        @Test
    public void testSetLastModified() throws IOException {
        long time = 1316137362000L;
        File file = new File("target/foo.test");
        boolean created = file.createNewFile();
        boolean modified = file.setLastModified(time);

        assertTrue(created);
        assertTrue(modified);
        assertEquals(time, file.lastModified());
    }
}