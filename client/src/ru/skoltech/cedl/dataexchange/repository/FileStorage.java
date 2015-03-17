package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.structure.model.StudyModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by D.Knoll on 13.03.2015.
 */
public class FileStorage {

    public static void save(StudyModel study) throws IOException {

        try (FileOutputStream fos = new FileOutputStream(study.getFile())) {

            JAXBContext jc = JAXBContext.newInstance(SystemModel.class);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(study.getSystemModel(), fos);


        } catch (JAXBException e) {
            throw new IOException("Error wrtiting system model to XML file.", e);
        }
    }

    public static SystemModel open(String fileName) throws IOException {
        try (FileInputStream inp = new FileInputStream(fileName)) {
            JAXBContext ct = JAXBContext.newInstance(SystemModel.class);

            Unmarshaller u = ct.createUnmarshaller();
            SystemModel sysMod = (SystemModel) u.unmarshal(inp);
            return sysMod;
        } catch (JAXBException e) {
            throw new IOException("Error reading system model from XML file.", e);
        }
    }
}
