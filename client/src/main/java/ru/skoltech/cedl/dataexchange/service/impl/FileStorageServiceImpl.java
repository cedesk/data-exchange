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

package ru.skoltech.cedl.dataexchange.service.impl;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.*;
import ru.skoltech.cedl.dataexchange.entity.calculation.Argument;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.model.*;
import ru.skoltech.cedl.dataexchange.entity.unit.Prefix;
import ru.skoltech.cedl.dataexchange.entity.unit.QuantityKind;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.entity.unit.UnitManagement;
import ru.skoltech.cedl.dataexchange.entity.user.Discipline;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.service.ExternalModelService;
import ru.skoltech.cedl.dataexchange.service.FileStorageService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of service which handles operations with file system.
 * <p>
 * Created by D.Knoll on 13.03.2015.
 */
public class FileStorageServiceImpl implements FileStorageService {

    private static final Class[] MODEL_CLASSES = new Class[]{
            SystemModel.class, SubSystemModel.class, ElementModel.class, InstrumentModel.class,
            ParameterModel.class, ExternalModel.class, ExternalModelReference.class, Calculation.class, Argument.class};
    private static Logger logger = Logger.getLogger(FileStorageServiceImpl.class);

    private ExternalModelService externalModelService;

    private final File applicationDirectory;

    /**
     * Service defines an application directory at the start up.
     * <p>
     * If <i>cedesk.app.dir</i> property defined as absolute path then use it.
     * Id <i>cedesk.app.dir</i> property defined as relative path, then prepend <i>user.home</i> system property to it.
     * TODO: write a test
     *
     * @param applicationSettings application settings to access to <i>cedesk.app.dir</i> property
     */
    public FileStorageServiceImpl(ApplicationSettings applicationSettings) {
        File cedeskAppDir = new File(applicationSettings.getCedeskAppDir());
        if (cedeskAppDir.isAbsolute()) {
            this.applicationDirectory = cedeskAppDir;
        } else {
            String userHome = System.getProperty("user.home");
            this.applicationDirectory = new File(userHome, applicationSettings.getCedeskAppDir());
        }
        if (!this.applicationDirectory.exists()) {
            boolean created = this.applicationDirectory.mkdirs();
            if (!created) {
                logger.error("unable to create application directory: " + this.applicationDirectory.getAbsolutePath());
            }
        }
    }

    public void setExternalModelService(ExternalModelService externalModelService) {
        this.externalModelService = externalModelService;
    }

    @Override
    public File applicationDirectory() {
        return applicationDirectory;
    }

    @Override
    public boolean checkFileExistenceAndNonEmptiness(File file) {
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                if (br.readLine() == null) {
                    return false;
                }
            } catch (IOException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void createDirectory(File path) {
        if (!path.exists()) {
            logger.info("Creating directory: " + path.toString());
            boolean created = path.mkdirs();
            if (!created) {
                logger.error("unable to create directory: " + path.getAbsolutePath());
            }
        }
        if (!path.canRead() || !path.canWrite()) {
            logger.error("Warning: Directory is not usable: " + path.toString());
        }
    }

    @Override
    public File dataDir(String repositoryUrl, String repositoryScheme, String projectName) {
        File repoDir = new File(this.applicationDirectory(), repositoryUrl);
        File schemaDir = new File(repoDir, repositoryScheme);
        return new File(schemaDir, projectName);
    }

    @Override
    public Calculation importCalculation(File inputFile) throws IOException {
        try (FileInputStream inp = new FileInputStream(inputFile)) {
            final Class[] MC = Calculation.getEntityClasses();
            JAXBContext ct = JAXBContext.newInstance(MC);

            Unmarshaller u = ct.createUnmarshaller();

            return (Calculation) u.unmarshal(inp);
        } catch (JAXBException e) {
            throw new IOException("Error reading calculation from XML file.", e);
        }
    }

    @Override
    public Study importStudy(File inputFile) throws IOException {
        try (FileInputStream inp = new FileInputStream(inputFile)) {
            Set<Class> modelClasses = new HashSet<>();
            modelClasses.add(Study.class);
            modelClasses.addAll(Arrays.asList(UserRoleManagement.class, User.class, Discipline.class));
            modelClasses.addAll(Arrays.asList(MODEL_CLASSES));
            modelClasses.addAll(Arrays.asList(Calculation.getEntityClasses()));
            JAXBContext jc = JAXBContext.newInstance(modelClasses.toArray(new Class[]{}));

            Unmarshaller u = jc.createUnmarshaller();
            Study study = (Study) u.unmarshal(inp);

            File inputFolder = inputFile.getParentFile();

            this.postProcessUserRoleManagement(study.getUserRoleManagement());
            this.postProcessStudy(study);
            this.postProcessSystemModel(study.getSystemModel(), null, inputFolder);
            return study;
        } catch (JAXBException e) {
            throw new IOException("Error reading study from XML file.", e);
        }
    }

    @Override
    public SystemModel importSystemModel(File inputFile) throws IOException {
        try (FileInputStream inp = new FileInputStream(inputFile)) {
            Set<Class> modelClasses = new HashSet<>();
            modelClasses.addAll(Arrays.asList(MODEL_CLASSES));
            modelClasses.addAll(Arrays.asList(Calculation.getEntityClasses()));
            JAXBContext jc = JAXBContext.newInstance(modelClasses.toArray(new Class[]{}));

            Unmarshaller u = jc.createUnmarshaller();
            SystemModel systemModel = (SystemModel) u.unmarshal(inp);

            File inputFolder = inputFile.getParentFile();

            postProcessSystemModel(systemModel, null, inputFolder);
            return systemModel;
        } catch (JAXBException e) {
            throw new IOException("Error reading system model from XML file.", e);
        }
    }

    @Override
    public UnitManagement importUnitManagement(InputStream inputStream) throws IOException {
        try (BufferedInputStream inp = new BufferedInputStream(inputStream)) {
            JAXBContext ct = JAXBContext.newInstance(UnitManagement.class, Prefix.class, Unit.class, QuantityKind.class);

            Unmarshaller u = ct.createUnmarshaller();
            UnitManagement unitManagement = (UnitManagement) u.unmarshal(inp);
            postProcessUnitManagement(unitManagement);
            return unitManagement;
        } catch (JAXBException e) {
            throw new IOException("Error reading unit management from XML file.", e);
        }
    }

    @Override
    public UserRoleManagement importUserRoleManagement(File inputFile) throws IOException {
        try (FileInputStream inp = new FileInputStream(inputFile)) {
            JAXBContext ct = JAXBContext.newInstance(UserRoleManagement.class, User.class, Discipline.class);

            Unmarshaller u = ct.createUnmarshaller();
            return (UserRoleManagement) u.unmarshal(inp);
        } catch (JAXBException e) {
            throw new IOException("Error reading user management from XML file.", e);
        }
    }

    @Override
    public void exportCalculation(Calculation calculation, File outputFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            final Class[] MC = Calculation.getEntityClasses();
            JAXBContext jc = JAXBContext.newInstance(MC);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(calculation, fos);
        } catch (JAXBException e) {
            throw new IOException("Error writing system model to XML file.", e);
        }
    }

    @Override
    public void exportStudy(Study study, File outputFile) throws IOException {
        File outputFolder = outputFile.getParentFile();
        this.createDirectory(outputFolder);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            Set<Class> modelClasses = new HashSet<>();
            modelClasses.add(Study.class);
            modelClasses.addAll(Arrays.asList(UserRoleManagement.class, User.class, Discipline.class));
            modelClasses.addAll(Arrays.asList(MODEL_CLASSES));
            modelClasses.addAll(Arrays.asList(Calculation.getEntityClasses()));
            JAXBContext jc = JAXBContext.newInstance(modelClasses.toArray(new Class[]{}));

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(study, fos);
        } catch (JAXBException e) {
            throw new IOException("Error writing study to XML file.", e);
        }

        this.storeExternalModels(study.getSystemModel(), outputFolder);
    }

    @Override
    public void exportSystemModel(SystemModel systemModel, File outputFile) throws IOException {
        File outputFolder = outputFile.getParentFile();
        this.createDirectory(outputFolder);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            Set<Class> modelClasses = new HashSet<>();
            modelClasses.addAll(Arrays.asList(MODEL_CLASSES));
            modelClasses.addAll(Arrays.asList(Calculation.getEntityClasses()));
            JAXBContext jc = JAXBContext.newInstance(modelClasses.toArray(new Class[]{}));

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(systemModel, fos);
        } catch (JAXBException e) {
            throw new IOException("Error writing system model to XML file.", e);
        }

        this.storeExternalModels(systemModel, outputFolder);
    }

    private void storeExternalModels(SystemModel systemModel, File outputFolder) throws IOException {
        Iterator<ExternalModel> iterator = systemModel.externalModelsIterator();
        while (iterator.hasNext()) {
            ExternalModel externalModel = iterator.next();
            String nodePath = externalModelService.makeExternalModelPath(externalModel);
            File nodeDir = new File(outputFolder, nodePath);
            this.createDirectory(nodeDir);
            externalModelService.storeExternalModel(externalModel, nodeDir);
        }
    }

    @Override
    public void exportUnitManagement(UnitManagement unitManagement, File outputFile) throws IOException {
        this.createDirectory(outputFile.getParentFile());

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {

            JAXBContext jc = JAXBContext.newInstance(UnitManagement.class, Prefix.class, Unit.class, QuantityKind.class);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(unitManagement, fos);
        } catch (JAXBException e) {
            throw new IOException("Error writing unit management to XML file.", e);
        }
    }

    @Override
    public void exportUserRoleManagement(UserRoleManagement userRoleManagement, File outputFile) throws IOException {
        this.createDirectory(outputFile.getParentFile());

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {

            JAXBContext jc = JAXBContext.newInstance(UserRoleManagement.class, User.class, Discipline.class);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(userRoleManagement, fos);
        } catch (JAXBException e) {
            throw new IOException("Error writing user management to XML file.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void postProcessSystemModel(ModelNode modelNode, CompositeModelNode parent, File inputFolder) {
        modelNode.setParent(parent);
        for (ExternalModel externalModel : modelNode.getExternalModels()) {
            externalModel.setParent(modelNode);
            try {
                String nodePath = externalModelService.makeExternalModelPath(externalModel);
                File nodeDir = new File(inputFolder, nodePath);
                File file = new File(nodeDir, externalModel.getName());
                if (file.exists()) {
                    externalModelService.updateExternalModelFromFile(file, externalModel);
                } else {
                    logger.error("external model file not found!");
                }
            } catch (Exception e) {
                logger.error("external model file import failed!", e);
            }
        }

        for (ParameterModel parameterModel : modelNode.getParameters()) {
            parameterModel.setParent(modelNode);

            if (parameterModel.getValueReference() != null && parameterModel.getValueReference().getExternalModel() != null) {
                String externalModelName = parameterModel.getValueReference().getExternalModel().getName();
                parameterModel.getValueReference().setExternalModel(modelNode.getExternalModelMap().get(externalModelName));
            }
            if (parameterModel.getExportReference() != null && parameterModel.getExportReference().getExternalModel() != null) {
                String externalModelName = parameterModel.getExportReference().getExternalModel().getName();
                parameterModel.getExportReference().setExternalModel(modelNode.getExternalModelMap().get(externalModelName));
            }
            if (parameterModel.getValueSource() == ParameterValueSource.LINK) {
                logger.info(parameterModel.getNodePath() + " <L- " + (parameterModel.getValueLink() != null ? parameterModel.getValueLink().getNodePath() : "null"));
            }
            if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE) {
                logger.info(parameterModel.getNodePath() + " <R= " + parameterModel.getValueReference());
            }
            if (parameterModel.getIsExported()) {
                logger.info(parameterModel.getNodePath() + " =E> " + parameterModel.getExportReference());
            }
            if (parameterModel.getUnit() == null) {
                logger.warn("parameter " + parameterModel.getNodePath() + " is missing a unit!");
            }
            Calculation calculation = parameterModel.getCalculation();
            if (calculation != null && calculation.getArguments() != null) {
                for (Argument argument : calculation.getArguments()) {
                    argument.setParent(calculation);
                }
            }
        }
        if (modelNode instanceof CompositeModelNode) {
            CompositeModelNode compositeModelNode = (CompositeModelNode) modelNode;
            for (Object node : compositeModelNode.getSubNodes()) {
                postProcessSystemModel((ModelNode) node, compositeModelNode, inputFolder);
            }
        }
    }

    private void postProcessStudy(Study study) {
        Map<String, SubSystemModel> subSystemModels = study.getSystemModel().getSubNodes()
                .stream().collect(Collectors.toMap(SubSystemModel::getName, subSystemModel -> subSystemModel));
        study.getUserRoleManagement().getDisciplineSubSystems().forEach(disciplineSubSystem -> {
            String subSystemName = disciplineSubSystem.getSubSystem().getName();
            SubSystemModel subSystemModel = subSystemModels.get(subSystemName);
            disciplineSubSystem.setSubSystem(subSystemModel);
        });
    }

    private void postProcessUserRoleManagement(UserRoleManagement userRoleManagement) {
        Map<String, Discipline> disciplines = userRoleManagement.getDisciplines()
                .stream().collect(Collectors.toMap(Discipline::getName, discipline -> discipline));

        userRoleManagement.getDisciplines().forEach(discipline -> discipline.setUserRoleManagement(userRoleManagement));
        userRoleManagement.getUserDisciplines().forEach(userDiscipline -> {
            String disciplineName = userDiscipline.getDiscipline().getName();
            Discipline discipline = disciplines.get(disciplineName);
            userDiscipline.setDiscipline(discipline);
            userDiscipline.setUserRoleManagement(userRoleManagement);
        });

        userRoleManagement.getDisciplineSubSystems().forEach(disciplineSubSystem -> {
            String disciplineName = disciplineSubSystem.getDiscipline().getName();
            Discipline discipline = disciplines.get(disciplineName);
            disciplineSubSystem.setDiscipline(discipline);
            disciplineSubSystem.setUserRoleManagement(userRoleManagement);
        });
    }

    private void postProcessUnitManagement(UnitManagement unitManagement) {
        for (Unit unit : unitManagement.getUnits()) {
            String quantityKindStr = unit.getQuantityKindStr();
            if (quantityKindStr != null) {
                Integer qtki = Integer.valueOf(quantityKindStr);
                QuantityKind quantityKind = unitManagement.getQuantityKinds().get(qtki);
                unit.setQuantityKind(quantityKind);
            }
        }
    }

}
