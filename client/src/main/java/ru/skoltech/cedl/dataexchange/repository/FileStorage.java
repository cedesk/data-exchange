package ru.skoltech.cedl.dataexchange.repository;

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

/**
 * Created by D.Knoll on 13.03.2015.
 */
public class FileStorage {

    public FileStorage() {
    }

    public void storeSystemModel(SystemModel systemModel, File outputFile) throws IOException {

        StorageUtils.makeDirectory(outputFile.getParentFile());

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {

            JAXBContext jc = JAXBContext.newInstance(SystemModel.class, SubSystemModel.class, ElementModel.class, InstrumentModel.class);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(systemModel, fos);
        } catch (JAXBException e) {
            throw new IOException("Error writing system model to XML file.", e);
        }
    }

    public SystemModel loadSystemModel(File inputFile) throws IOException {
        try (FileInputStream inp = new FileInputStream(inputFile)) {
            JAXBContext ct = JAXBContext.newInstance(SystemModel.class, SubSystemModel.class, ElementModel.class, InstrumentModel.class);

            Unmarshaller u = ct.createUnmarshaller();
            SystemModel systemModel = (SystemModel) u.unmarshal(inp);

            postProcessSystemModel(systemModel, null);
            return systemModel;
        } catch (JAXBException e) {
            throw new IOException("Error reading system model from XML file.", e);
        }
    }

    private void postProcessSystemModel(ModelNode modelNode, ModelNode parent) {
        modelNode.setParent(parent);
        for (ParameterModel parameterModel : modelNode.getParameters()) {
            parameterModel.setParent(modelNode);
        }
        for (ExternalModel externalModel : modelNode.getExternalModels()) {
            externalModel.setParent(modelNode);
        }
        if (modelNode instanceof CompositeModelNode) {
            CompositeModelNode compositeModelNode = (CompositeModelNode) modelNode;
            for (Object node : compositeModelNode.getSubNodes()) {
                postProcessSystemModel((ModelNode) node, modelNode);
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
