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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.calculation.Argument;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.model.*;
import ru.skoltech.cedl.dataexchange.entity.unit.Prefix;
import ru.skoltech.cedl.dataexchange.entity.unit.QuantityKind;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.entity.user.Discipline;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.repository.jpa.PrefixRepository;
import ru.skoltech.cedl.dataexchange.repository.jpa.QuantityKindRepository;
import ru.skoltech.cedl.dataexchange.repository.jpa.UnitRepository;
import ru.skoltech.cedl.dataexchange.service.ExternalModelService;
import ru.skoltech.cedl.dataexchange.service.FileStorageService;
import ru.skoltech.cedl.dataexchange.service.UserService;
import ru.skoltech.cedl.dataexchange.structure.adapters.QuantityKindAdapter;
import ru.skoltech.cedl.dataexchange.structure.adapters.UnitAdapter;
import ru.skoltech.cedl.dataexchange.structure.adapters.UnitWrapper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Implementation of service which handles operations with file system.
 * <p>
 * Created by D.Knoll on 13.03.2015.
 */
public class FileStorageServiceImpl implements FileStorageService {

    private static final Class[] MODEL_CLASSES = new Class[]{
            SystemModel.class, SubSystemModel.class, ElementModel.class, InstrumentModel.class,
            ParameterModel.class, ExternalModel.class, Calculation.class, Argument.class};
    private static Logger logger = Logger.getLogger(FileStorageServiceImpl.class);

    private final UnitRepository unitRepository;
    private final QuantityKindRepository quantityKindRepository;
    private final PrefixRepository prefixRepository;

    private ApplicationSettings applicationSettings;
    private UserService userService;
    private ExternalModelService externalModelService;

    private UnitAdapter unitAdapter;
    private QuantityKindAdapter quantityKindAdapter;

    @Autowired
    public FileStorageServiceImpl(UnitRepository unitRepository,
                                  QuantityKindRepository quantityKindRepository,
                                  PrefixRepository prefixRepository) {
        this.unitRepository = unitRepository;
        this.quantityKindRepository = quantityKindRepository;
        this.prefixRepository = prefixRepository;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setExternalModelService(ExternalModelService externalModelService) {
        this.externalModelService = externalModelService;
    }

    public void setUnitAdapter(UnitAdapter unitAdapter) {
        this.unitAdapter = unitAdapter;
    }

    public void setQuantityKindAdapter(QuantityKindAdapter quantityKindAdapter) {
        this.quantityKindAdapter = quantityKindAdapter;
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
        File repoDir = new File(applicationSettings.applicationDirectory(), repositoryUrl);
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
    public Study importStudyFromZip(File inputFile) throws IOException {
        ZipFile zipFile = new ZipFile(inputFile);

        ZipEntry xmlZipEntry = Collections.list(zipFile.entries()).stream()
                .filter(o -> "xml".equals(FilenameUtils.getExtension(o.getName())))
                .findFirst().orElse(null);
        if (xmlZipEntry == null) {
            throw new IOException("File does not contain study to import.");
        }

        try (InputStream inputStream = zipFile.getInputStream(xmlZipEntry)) {
            Set<Class> modelClasses = new HashSet<>();
            modelClasses.add(Study.class);
            modelClasses.addAll(Arrays.asList(UserRoleManagement.class, User.class, Discipline.class));
            modelClasses.addAll(Arrays.asList(MODEL_CLASSES));
            modelClasses.addAll(Arrays.asList(Calculation.getEntityClasses()));
            JAXBContext jc = JAXBContext.newInstance(modelClasses.toArray(new Class[]{}));

            Unmarshaller unmarshaller = jc.createUnmarshaller();
            unmarshaller.setAdapter(UnitAdapter.class, unitAdapter);
            Study study = (Study) unmarshaller.unmarshal(inputStream);

            this.postProcessUserRoleManagement(study.getUserRoleManagement());
            this.postProcessStudy(study);
            this.postProcessSystemModel(study.getSystemModel(), null, zipFile);

            return study;
        } catch (JAXBException e) {
            throw new IOException("Error reading study from XML file.", e);
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

            Unmarshaller unmarshaller = jc.createUnmarshaller();
            unmarshaller.setAdapter(UnitAdapter.class, unitAdapter);
            Study study = (Study) unmarshaller.unmarshal(inp);

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

            Unmarshaller unmarshaller = jc.createUnmarshaller();
            unmarshaller.setAdapter(UnitAdapter.class, unitAdapter);
            SystemModel systemModel = (SystemModel) unmarshaller.unmarshal(inp);

            File inputFolder = inputFile.getParentFile();

            this.postProcessSystemModel(systemModel, null, inputFolder);
            return systemModel;
        } catch (JAXBException e) {
            throw new IOException("Error reading system model from XML file.", e);
        }
    }

    @Override
    public List<Prefix> importPrefixes(String resource) throws IOException {
        return importUnitsEntities(resource, "prefix", Prefix.class);
    }

    @Override
    public List<Unit> importUnits(String resource) throws IOException {
        return importUnitsEntities(resource, "unit", Unit.class);
    }

    @Override
    public List<QuantityKind> importQuantityKinds(String resource) throws IOException {
        return importUnitsEntities(resource, "quantityKind", QuantityKind.class);
    }

    private <T> List<T> importUnitsEntities(String resource, String elementName, Class<T> clazz) throws IOException {
        try (InputStream inp = FileStorageServiceImpl.class.getClassLoader().getResourceAsStream(resource)) {
            XMLInputFactory xif = XMLInputFactory.newFactory();
            XMLStreamReader xsr = xif.createXMLStreamReader(inp);
            List<T> entities = new LinkedList<>();
            while (xsr.hasNext()) {
                int eventType = xsr.next();
                switch (eventType) {
                    case XMLStreamReader.START_ELEMENT:
                        String localName = xsr.getLocalName();
                        if (elementName.equalsIgnoreCase(localName)) {
                            JAXBContext jc = JAXBContext.newInstance(clazz);
                            Unmarshaller unmarshaller = jc.createUnmarshaller();
                            unmarshaller.setAdapter(QuantityKindAdapter.class, quantityKindAdapter);

                            T entity = unmarshaller.unmarshal(xsr, clazz).getValue();
                            entities.add(entity);
                        }
                    case XMLStreamReader.END_DOCUMENT:
                        break;
                }
            }
            xsr.close();
            return entities;
        } catch (JAXBException | XMLStreamException e) {
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
    public void exportStudyToZip(Study study, File outputFile) throws IOException {
        File outputFolder = outputFile.getParentFile();
        this.createDirectory(outputFolder);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            try (ZipOutputStream zos = new ZipOutputStream(fos)) {
                String xmlFileName = FilenameUtils.getBaseName(outputFile.getName());
                ZipEntry studyXmlZipEntry = new ZipEntry(xmlFileName + ".xml");
                zos.putNextEntry(studyXmlZipEntry);

                Set<Class> modelClasses = new HashSet<>();
                modelClasses.add(Study.class);
                modelClasses.addAll(Arrays.asList(UserRoleManagement.class, User.class, Discipline.class));
                modelClasses.addAll(Arrays.asList(MODEL_CLASSES));
                modelClasses.addAll(Arrays.asList(Calculation.getEntityClasses()));
                JAXBContext jc = JAXBContext.newInstance(modelClasses.toArray(new Class[]{}));

                Marshaller marshaller = jc.createMarshaller();
                marshaller.setAdapter(UnitAdapter.class, unitAdapter);
                marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller.marshal(study, zos);

                Iterator<ExternalModel> iterator = study.getSystemModel().externalModelsIterator();
                Set<String> createdExternalModelPaths = new HashSet<>();
                while (iterator.hasNext()) {
                    ExternalModel externalModel = iterator.next();
                    String externalModelPath = externalModelService.makeExternalModelZipPath(externalModel);
                    String externalModelZipEntryName = externalModelPath + externalModel.getName();

                    if (!createdExternalModelPaths.contains(externalModelPath)) {
                        ZipEntry nodePathZipEntry = new ZipEntry(externalModelPath);
                        zos.putNextEntry(nodePathZipEntry);
                        createdExternalModelPaths.add(externalModelPath);
                    }

                    ZipEntry externalModelZipEntry = new ZipEntry(externalModelZipEntryName);
                    externalModelZipEntry.setSize(externalModel.getAttachment().length);
                    zos.putNextEntry(externalModelZipEntry);
                    zos.write(externalModel.getAttachment());
                    zos.closeEntry();
                }
                zos.flush();
            }
        } catch (JAXBException e) {
            throw new IOException("Error writing study to XML file.", e);
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

            Marshaller marshaller = jc.createMarshaller();
            marshaller.setAdapter(UnitAdapter.class, unitAdapter);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(study, fos);
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

            Marshaller marshaller = jc.createMarshaller();
            marshaller.setAdapter(UnitAdapter.class, unitAdapter);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(systemModel, fos);
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

            File externalModelFile = new File(nodeDir, externalModel.getName());
            Files.write(externalModelFile.toPath(), externalModel.getAttachment(), StandardOpenOption.CREATE);
        }
    }

    @Override
    public void exportUnits(File outputFile) throws IOException {
        this.createDirectory(outputFile.getParentFile());

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {

            JAXBContext jc = JAXBContext.newInstance(UnitWrapper.class, Prefix.class, Unit.class, QuantityKind.class);

            Marshaller marshaller = jc.createMarshaller();
            marshaller.setAdapter(QuantityKindAdapter.class, quantityKindAdapter);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            List<Prefix> prefixList = prefixRepository.findAll();
            List<Unit> unitList = unitRepository.findAll();
            List<QuantityKind> quantityKindList = quantityKindRepository.findAll();

            UnitWrapper unitWrapper = new UnitWrapper();
            unitWrapper.setPrefixes(prefixList);
            unitWrapper.setUnits(unitList);
            unitWrapper.setQuantityKinds(quantityKindList);
            marshaller.marshal(unitWrapper, fos);
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
    private void postProcessSystemModel(ModelNode modelNode, CompositeModelNode parent, ZipFile zipFile) {
        modelNode.setParent(parent);
        for (ExternalModel externalModel : modelNode.getExternalModels()) {
            externalModel.setParent(modelNode);
            try {
                String externalModelPath = externalModelService.makeExternalModelZipPath(externalModel);
                String externalModelZipEntryName = externalModelPath + externalModel.getName();
                ZipEntry externalModelZipEntry = zipFile.getEntry(externalModelZipEntryName);
                if (externalModelZipEntry != null) {
                    try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                        try (InputStream is = zipFile.getInputStream(externalModelZipEntry)) {
                            IOUtils.copy(is, os);
                            externalModel.setAttachment(os.toByteArray());
                            externalModel.init();
                        }
                    }
                } else {
                    logger.error("external model file not found!");
                }
            } catch (Exception e) {
                logger.error("external model file import failed!", e);
            }
        }

        this.postProcessParameterModels(modelNode);

        if (modelNode instanceof CompositeModelNode) {
            CompositeModelNode compositeModelNode = (CompositeModelNode) modelNode;
            for (Object node : compositeModelNode.getSubNodes()) {
                postProcessSystemModel((ModelNode) node, compositeModelNode, zipFile);
            }
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

        this.postProcessParameterModels(modelNode);

        if (modelNode instanceof CompositeModelNode) {
            CompositeModelNode compositeModelNode = (CompositeModelNode) modelNode;
            for (Object node : compositeModelNode.getSubNodes()) {
                postProcessSystemModel((ModelNode) node, compositeModelNode, inputFolder);
            }
        }
    }

    private void postProcessParameterModels(ModelNode modelNode) {
        for (ParameterModel parameterModel : modelNode.getParameters()) {
            parameterModel.setParent(modelNode);

            if (parameterModel.getImportModel() != null) {
                String externalModelName = parameterModel.getImportModel().getName();
                parameterModel.setImportModel(modelNode.getExternalModelMap().get(externalModelName));
            }
            if (parameterModel.getExportModel() != null) {
                String externalModelName = parameterModel.getExportModel().getName();
                parameterModel.setExportModel(modelNode.getExternalModelMap().get(externalModelName));
            }
            if (parameterModel.getValueSource() == ParameterValueSource.LINK) {
                logger.info(parameterModel.getNodePath() + " <L- " + (parameterModel.getValueLink() != null ? parameterModel.getValueLink().getNodePath() : "null"));
            }
            if (parameterModel.getValueSource() == ParameterValueSource.REFERENCE) {
                String valueReference = parameterModel.getImportModel() != null
                        ? parameterModel.getImportModel().getName() + ":" + parameterModel.getImportField() : "(empty)";
                logger.info(parameterModel.getNodePath() + " <R= " + valueReference);
            }
            if (parameterModel.getIsExported()) {
                String exportReference = parameterModel.getExportModel() != null
                        ? parameterModel.getExportModel().getName() + ":" + parameterModel.getExportField() : "(empty)";
                logger.info(parameterModel.getNodePath() + " =E> " + exportReference);
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
            User user = userService.findUser(userDiscipline.getUser().getUserName());
            userDiscipline.setUser(user);
            userDiscipline.setDiscipline(discipline);
            userDiscipline.setUserRoleManagement(userRoleManagement);
        });

        userRoleManagement.getUserDisciplines().removeIf(userDiscipline -> userDiscipline.getUser() == null);

        userRoleManagement.getDisciplineSubSystems().forEach(disciplineSubSystem -> {
            String disciplineName = disciplineSubSystem.getDiscipline().getName();
            Discipline discipline = disciplines.get(disciplineName);
            disciplineSubSystem.setDiscipline(discipline);
            disciplineSubSystem.setUserRoleManagement(userRoleManagement);
        });
    }

}
