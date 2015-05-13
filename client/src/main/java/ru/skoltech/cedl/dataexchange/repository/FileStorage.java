package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.structure.model.ElementModel;
import ru.skoltech.cedl.dataexchange.structure.model.InstrumentModel;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

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

    public static void store(SystemModel systemModel, File outputFile) throws IOException {

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

    public static SystemModel load(File inputFile) throws IOException {
        try (FileInputStream inp = new FileInputStream(inputFile)) {
            JAXBContext ct = JAXBContext.newInstance(SystemModel.class, SubSystemModel.class, ElementModel.class, InstrumentModel.class);

            Unmarshaller u = ct.createUnmarshaller();
            SystemModel systemModel = (SystemModel) u.unmarshal(inp);
            return systemModel;

        } catch (JAXBException e) {
            throw new IOException("Error reading system model from XML file.", e);
        }
    }

    public static void store(UserManagement userManagement, File outputFile) throws IOException {

        StorageUtils.makeDirectory(outputFile.getParentFile());

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {

            JAXBContext jc = JAXBContext.newInstance(UserManagement.class, User.class, Discipline.class);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(userManagement, fos);

        } catch (JAXBException e) {
            throw new IOException("Error writing system model to XML file.", e);
        }
    }
}
