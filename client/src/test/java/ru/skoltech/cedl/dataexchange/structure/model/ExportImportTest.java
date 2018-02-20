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
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.ext.ExcelExternalModel;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.unit.Prefix;
import ru.skoltech.cedl.dataexchange.entity.unit.QuantityKind;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.entity.user.Discipline;
import ru.skoltech.cedl.dataexchange.entity.user.DisciplineSubSystem;
import ru.skoltech.cedl.dataexchange.entity.user.UserDiscipline;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.external.ExternalModelException;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.jpa.PrefixRepository;
import ru.skoltech.cedl.dataexchange.repository.jpa.QuantityKindRepository;
import ru.skoltech.cedl.dataexchange.repository.jpa.UnitRepository;
import ru.skoltech.cedl.dataexchange.service.ExternalModelService;
import ru.skoltech.cedl.dataexchange.service.FileStorageService;
import ru.skoltech.cedl.dataexchange.service.UnitService;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.structure.BasicSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Created by D.Knoll on 13.05.2015.
 */
public class ExportImportTest extends AbstractApplicationContextTest {

    private PrefixRepository prefixRepository;
    private UnitRepository unitRepository;
    private QuantityKindRepository quantityKindRepository;

    private SystemBuilder systemBuilder;
    private FileStorageService fileStorageService;
    private ExternalModelService externalModelService;
    private UnitService unitService;

    private File excelAttachmentFile, csvAttachmentFile;
    private Study originalStudy;
    private ExternalModel excelExternalModel, csvExternalModel;

    @Before
    public void setup() throws URISyntaxException, ExternalModelException {
        prefixRepository = context.getBean(PrefixRepository.class);
        unitRepository = context.getBean(UnitRepository.class);
        quantityKindRepository = context.getBean(QuantityKindRepository.class);

        excelAttachmentFile = new File(ExportImportTest.class.getResource("/attachment.xls").toURI());
        csvAttachmentFile = new File(ExportImportTest.class.getResource("/attachment.csv").toURI());

        systemBuilder = context.getBean(BasicSpaceSystemBuilder.class);
        systemBuilder.modelDepth(1);
        fileStorageService = context.getBean(FileStorageService.class);
        externalModelService = context.getBean(ExternalModelService.class);
        unitService = context.getBean(UnitService.class);
        UserRoleManagementService userRoleManagementService = context.getBean(UserRoleManagementService.class);

        systemBuilder.modelDepth(2);
        SystemModel originalSystemModel = systemBuilder.build("testModel");
        UserRoleManagement userRoleManagement = userRoleManagementService.createUserRoleManagementWithSubsystemDisciplines(originalSystemModel);

        excelExternalModel = externalModelService.createExternalModelFromFile(excelAttachmentFile, originalSystemModel);
        csvExternalModel = externalModelService.createExternalModelFromFile(csvAttachmentFile, originalSystemModel);
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

        String studyName = "studyName";
        originalStudy = new Study();
        originalStudy.setName(studyName);
        originalStudy.setSystemModel(originalSystemModel);
        originalStudy.setUserRoleManagement(userRoleManagement);

        Discipline discipline1 = userRoleManagement.getDisciplines().get(0);
        Discipline discipline2 = userRoleManagement.getDisciplines().get(1);
        SubSystemModel subSystemModel = originalSystemModel.getSubNodes().get(0);
        SubSystemModel subSystemMode2 = originalSystemModel.getSubNodes().get(1);
        boolean added1 = userRoleManagementService.addDisciplineSubsystem(userRoleManagement, discipline1, subSystemModel);
        boolean added2 = userRoleManagementService.addDisciplineSubsystem(userRoleManagement, discipline2, subSystemMode2);
        assertTrue(added1);
        assertTrue(added2);
    }

    @Test
    public void testExportImportStudyZip() throws IOException {
        File file = new File("target", "dummy-study.zip");
        fileStorageService.exportStudyToZip(originalStudy, file);
        Study importedStudy = fileStorageService.importStudyFromZip(file);
        this.checkImportedStudy(importedStudy);
        file.deleteOnExit();
    }

    @Test
    public void testExportImportStudy() throws IOException {
        File file = new File("target", "dummy-study.xml");
        fileStorageService.exportStudy(originalStudy, file);
        Study importedStudy = fileStorageService.importStudy(file);
        this.checkImportedStudy(importedStudy);
        file.deleteOnExit();
    }

    private void checkImportedStudy(Study importedStudy) {
        //        assertEquals(originalStudy, importedStudy);
        assertEquals(originalStudy.getName(), importedStudy.getName());
        assertThat(importedStudy.getSystemModel().getExternalModels(), hasItem(excelExternalModel));
        assertThat(importedStudy.getSystemModel().getExternalModels(), hasItem(csvExternalModel));
        importedStudy.getSystemModel().getExternalModels()
                .forEach(externalModel -> assertTrue(externalModel.state().isInitialized()));

        Discipline[] userDisciplines = importedStudy.getUserRoleManagement().getUserDisciplines()
                .stream().map(UserDiscipline::getDiscipline).toArray(Discipline[]::new);
        Discipline[] subsystemDisciplines = importedStudy.getUserRoleManagement().getDisciplineSubSystems()
                .stream().map(DisciplineSubSystem::getDiscipline).toArray(Discipline[]::new);
        SubSystemModel[] disciplineSubSystemModels = importedStudy.getUserRoleManagement().getDisciplineSubSystems()
                .stream().map(DisciplineSubSystem::getSubSystem).toArray(SubSystemModel[]::new);

        assertThat(importedStudy.getUserRoleManagement().getDisciplines(), hasItems(userDisciplines));
        assertThat(importedStudy.getUserRoleManagement().getDisciplines(), hasItems(subsystemDisciplines));
        assertThat(importedStudy.getSystemModel().getSubNodes(), hasItems(disciplineSubSystemModels));

        importedStudy.getUserRoleManagement().getDisciplines()
                .forEach(discipline -> assertEquals(importedStudy.getUserRoleManagement(), discipline.getUserRoleManagement()));
        importedStudy.getUserRoleManagement().getUserDisciplines()
                .forEach(userDiscipline -> assertEquals(importedStudy.getUserRoleManagement(), userDiscipline.getUserRoleManagement()));
        importedStudy.getUserRoleManagement().getDisciplineSubSystems()
                .forEach(disciplineSubSystem -> assertEquals(importedStudy.getUserRoleManagement(), disciplineSubSystem.getUserRoleManagement()));
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

        File file = new File("target", "dummy-system-model.xml");
        fileStorageService.exportSystemModel(originalSystemModel, file);
        SystemModel importedSystemModel = fileStorageService.importSystemModel(file);

        assertEquals(originalSystemModel, importedSystemModel);
        assertThat(importedSystemModel.getExternalModels(), hasItem(excelExternalModel));
        assertThat(importedSystemModel.getExternalModels(), hasItem(csvExternalModel));
        importedSystemModel.getExternalModels()
                .forEach(externalModel -> assertTrue(externalModel.state().isInitialized()));

        file.deleteOnExit();
    }

    @Test
    public void testImportOldSystemModel() throws IOException, URISyntaxException {
        File file = new File(ExportImportTest.class.getResource("/model-old.xml").toURI());
        File modelFile = new File("target", "model-old.xml");
        Files.copy(file.toPath(), modelFile.toPath());

        SystemModel systemModel = fileStorageService.importSystemModel(modelFile);

        assertNotNull(systemModel);
        assertFalse(systemModel.getExternalModels().isEmpty());
        assertEquals(1, systemModel.getExternalModels().size());
        ExternalModel externalModel = systemModel.getExternalModels().get(0);
        assertNotNull(externalModel);
        assertTrue(externalModel instanceof ExcelExternalModel);
        assertTrue(externalModel.state().isInitialized());
        modelFile.deleteOnExit();
    }

    @Test
    public void testExportImportCalculation() {
//        Calculation calculation = new Calculation();
//        calculation.setArguments();
        //TODO
    }

    @Test
    public void testExportImportUnitManagement() throws IOException {
        unitService.createDefaultUnits();
        File unitManagementFile = new File("target/test-classes", "dummy-unit-management.xml");
        fileStorageService.exportUnits(unitManagementFile);

        List<Prefix> importedPrefixes = fileStorageService.importPrefixes("dummy-unit-management.xml");
        List<Unit> importedUnits = fileStorageService.importUnits("dummy-unit-management.xml");
        List<QuantityKind> importedQuantityKinds = fileStorageService.importQuantityKinds("dummy-unit-management.xml");

        assertThat(importedPrefixes, containsInAnyOrder(prefixRepository.findAll().toArray()));
        assertThat(importedUnits, containsInAnyOrder(unitRepository.findAll().toArray()));
        assertThat(importedQuantityKinds, containsInAnyOrder(quantityKindRepository.findAll().toArray()));
        unitManagementFile.deleteOnExit();
    }

    @Test
    public void testExportImportUserRoleManagement() throws IOException {
//        UserManagement userManagement = userService.findUserManagement();
//
//        UserRoleManagement userRoleManagement = userRoleManagementService.createDefaultUserRoleManagement(userManagement);
//        File userRoleManagementFile = new File("target", "dummy-user-role-management.xml");
//        fileStorageService.exportUserRoleManagement(userRoleManagement, userRoleManagementFile);
//        UserRoleManagement userRoleManagementImported = fileStorageService.importUserRoleManagement(userRoleManagementFile);
//        assertEquals(userRoleManagement, userRoleManagementImported);
    }
}
