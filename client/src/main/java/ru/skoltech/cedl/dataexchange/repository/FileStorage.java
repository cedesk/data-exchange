package ru.skoltech.cedl.dataexchange.repository;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.model.calculation.Argument;
import ru.skoltech.cedl.dataexchange.units.model.Prefix;
import ru.skoltech.cedl.dataexchange.units.model.QuantityKind;
import ru.skoltech.cedl.dataexchange.units.model.Unit;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by D.Knoll on 13.03.2015.
 */
public class FileStorage {

    public static final Class[] MODEL_CLASSES = new Class[]{
            SystemModel.class, SubSystemModel.class, ElementModel.class, InstrumentModel.class,
            ParameterModel.class, ExternalModel.class, ExternalModelReference.class, Calculation.class, Argument.class};
    private static Logger logger = Logger.getLogger(FileStorage.class);

    public FileStorage() {
    }

    public void storeSystemModel(SystemModel systemModel, File outputFile) throws IOException {

        File outputFolder = outputFile.getParentFile();
        StorageUtils.makeDirectory(outputFolder);

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

        Iterator<ExternalModel> iterator = systemModel.externalModelsIterator();
        while (iterator.hasNext()) {
            ExternalModel externalModel = iterator.next();
            String nodePath = ExternalModelFileHandler.makePath(externalModel);
            File nodeDir = new File(outputFolder, nodePath);
            StorageUtils.makeDirectory(nodeDir);
            ExternalModelFileHandler.toFile(externalModel, nodeDir);
        }
    }

    public SystemModel loadSystemModel(File inputFile) throws IOException {
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

    private void postProcessSystemModel(ModelNode modelNode, ModelNode parent, File inputFolder) {
        modelNode.setParent(parent);
        for (ExternalModel externalModel : modelNode.getExternalModels()) {
            externalModel.setParent(modelNode);
            try {
                String nodePath = ExternalModelFileHandler.makePath(externalModel);
                File nodeDir = new File(inputFolder, nodePath);
                File file = new File(nodeDir, externalModel.getName());
                if (file.exists()) {
                    ExternalModelFileHandler.readAttachmentFromFile(externalModel, file);
                } else {
                    logger.error("external model file not found!");
                }
            } catch (Exception e) {
                logger.error("external model file import failed!", e);
            }
        }

        for (ParameterModel parameterModel : modelNode.getParameters()) {
            parameterModel.setParent(modelNode);
            parameterModel.setValueReference(new ExternalModelReference(parameterModel.getImportModel(), parameterModel.getImportField()));
            parameterModel.setExportReference(new ExternalModelReference(parameterModel.getExportModel(), parameterModel.getExportField()));
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
                postProcessSystemModel((ModelNode) node, modelNode, inputFolder);
            }
        }
    }

    public void storeUserManagement(UserRoleManagement userRoleManagement, File outputFile) throws IOException {

        StorageUtils.makeDirectory(outputFile.getParentFile());

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

    public UserRoleManagement loadUserManagement(File inputFile) throws IOException {
        try (FileInputStream inp = new FileInputStream(inputFile)) {
            JAXBContext ct = JAXBContext.newInstance(UserRoleManagement.class, User.class, Discipline.class);

            Unmarshaller u = ct.createUnmarshaller();
            UserRoleManagement userRoleManagement = (UserRoleManagement) u.unmarshal(inp);
            return userRoleManagement;
        } catch (JAXBException e) {
            throw new IOException("Error reading user management from XML file.", e);
        }
    }

    public void storeUnitManagement(UnitManagement unitManagement, File outputFile) throws IOException {

        StorageUtils.makeDirectory(outputFile.getParentFile());

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

    public UnitManagement loadUnitManagement(InputStream inputStream) throws IOException {
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

    public void storeCalculation(Calculation calc, File outputFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            final Class[] MC = Calculation.getEntityClasses();
            JAXBContext jc = JAXBContext.newInstance(MC);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "");
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(calc, fos);
        } catch (JAXBException e) {
            throw new IOException("Error writing system model to XML file.", e);
        }
    }

    public Calculation loadCalculation(File file) throws IOException {
        try (FileInputStream inp = new FileInputStream(file)) {
            final Class[] MC = Calculation.getEntityClasses();
            JAXBContext ct = JAXBContext.newInstance(MC);

            Unmarshaller u = ct.createUnmarshaller();
            Calculation calculation = (Calculation) u.unmarshal(inp);

            return calculation;
        } catch (JAXBException e) {
            throw new IOException("Error reading calculation from XML file.", e);
        }
    }
}
