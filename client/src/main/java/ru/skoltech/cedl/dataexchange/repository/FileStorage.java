package ru.skoltech.cedl.dataexchange.repository;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by D.Knoll on 13.03.2015.
 */
public class FileStorage {

    public static final Class[] MODEL_CLASSES = new Class[]{
            SystemModel.class, SubSystemModel.class, ElementModel.class, InstrumentModel.class,
            ParameterModel.class, ExternalModel.class, ExternalModelReference.class};
    private static Logger logger = Logger.getLogger(FileStorage.class);

    public FileStorage() {
    }

    public void storeSystemModel(SystemModel systemModel, File outputFile) throws IOException {

        File outputFolder = outputFile.getParentFile();
        StorageUtils.makeDirectory(outputFolder);

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {

            JAXBContext jc = JAXBContext.newInstance(MODEL_CLASSES);

            Marshaller m = jc.createMarshaller();
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
            JAXBContext ct = JAXBContext.newInstance(MODEL_CLASSES);

            Unmarshaller u = ct.createUnmarshaller();
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
                    ExternalModelFileHandler.updateFromFile(externalModel, file);
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileStorage{");
        sb.append('}');
        return sb.toString();
    }
}
