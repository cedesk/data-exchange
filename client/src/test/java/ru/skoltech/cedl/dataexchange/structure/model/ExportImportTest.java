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

import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModelReference;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.ext.ExcelExternalModel;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.service.ExternalModelService;
import ru.skoltech.cedl.dataexchange.service.FileStorageService;
import ru.skoltech.cedl.dataexchange.structure.BasicSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.*;

/**
 * Created by D.Knoll on 13.05.2015.
 */
public class ExportImportTest extends AbstractApplicationContextTest {

    private File excelAttachmentFile, csvAttachmentFile;
    private SystemBuilder systemBuilder;
    private FileStorageService fileStorageService;
    private ExternalModelService externalModelService;

    @Before
    public void setup() throws IOException, NoSuchFieldException, IllegalAccessException, URISyntaxException {
        excelAttachmentFile = new File(ExportImportTest.class.getResource("/attachment.xls").toURI());
        csvAttachmentFile = new File(ExportImportTest.class.getResource("/attachment.csv").toURI());

        systemBuilder = context.getBean(BasicSpaceSystemBuilder.class);
        systemBuilder.modelDepth(1);
        fileStorageService = context.getBean(FileStorageService.class);
        externalModelService = context.getBean(ExternalModelService.class);
    }

    @Test
    public void testImportSystemModel() throws IOException, URISyntaxException {
        File file1 = new File(ExportImportTest.class.getResource("/model1.xml").toURI());
        SystemModel systemModel1 = fileStorageService.importSystemModel(file1);

        File otherFile1 = new File(ExportImportTest.class.getResource("/model1.xml").toURI());
        SystemModel otherSystemModel1 = fileStorageService.importSystemModel(otherFile1);

        File file2 = new File(ExportImportTest.class.getResource("/model2.xml").toURI());
        SystemModel systemModel2 = fileStorageService.importSystemModel(file2);

        assertEquals(systemModel1, otherSystemModel1);
        assertNotEquals(systemModel1, systemModel2);

        ModelNode missionNode1 = systemModel1.getSubNodesMap().get("Communication");
        ModelNode missionNode2 = systemModel2.getSubNodesMap().get("Communication2");

        systemModel1.removeSubNode(missionNode1);
        systemModel2.removeSubNode(missionNode2);

        assertEquals(systemModel1, systemModel2);
    }

    @Test
    public void testExportImportSystemModel() throws IOException, ExternalModelException {
        SystemModel originalSystemModel = systemBuilder.build("testModel");

        ExternalModel excelExternalModel = externalModelService.createExternalModelFromFile(excelAttachmentFile, originalSystemModel);
        ExternalModel csvExternalModel = externalModelService.createExternalModelFromFile(csvAttachmentFile, originalSystemModel);
        originalSystemModel.addExternalModel(excelExternalModel);
        originalSystemModel.addExternalModel(csvExternalModel);

        ExternalModelReference valueModelReference1 = new ExternalModelReference();
        valueModelReference1.setExternalModel(excelExternalModel);
        valueModelReference1.setTarget("B3");
        ExternalModelReference exportModelReference = new ExternalModelReference();
        exportModelReference.setExternalModel(excelExternalModel);
        exportModelReference.setTarget("D4");

        ParameterModel parameterModel1 = originalSystemModel.getParameters().get(0);
        parameterModel1.setValueReference(valueModelReference1);
        ParameterModel parameterModel2 = originalSystemModel.getParameters().get(1);
        parameterModel2.setExportReference(exportModelReference);

        File file = new File("target", "DummySystemModel.xml");
        fileStorageService.exportSystemModel(originalSystemModel, file);
        SystemModel importedSystemModel = fileStorageService.importSystemModel(file);

        assertEquals(originalSystemModel, importedSystemModel);
        assertThat(importedSystemModel.getExternalModels(), hasItem(excelExternalModel));
        assertThat(importedSystemModel.getExternalModels(), hasItem(csvExternalModel));
        importedSystemModel.getExternalModels()
                .forEach(externalModel -> assertTrue(externalModel.state().isInitialized()));
    }

    @Test
    public void testImportOldSystemModel() throws IOException, ExternalModelException, URISyntaxException {
        File file = new File(ExportImportTest.class.getResource("/model-old.xml").toURI());
        SystemModel systemModel = fileStorageService.importSystemModel(file);

        assertNotNull(systemModel);
        assertFalse(systemModel.getExternalModels().isEmpty());
        assertEquals(1, systemModel.getExternalModels().size());
        ExternalModel externalModel = systemModel.getExternalModels().get(0);
        assertNotNull(externalModel);
        assertTrue(externalModel instanceof ExcelExternalModel);
        assertTrue(externalModel.state().isInitialized());
    }

    @Test
    public void testExportImportCalculation() {
//        Calculation calculation = new Calculation();
//        calculation.setArguments();
        //TODO
    }

    @Test
    public void testExportImportUnitManagement() {
        //TODO
    }

    @Test
    public void testExportImportUserRoleManagement() {
        //TODO
    }


}
