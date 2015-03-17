package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.structure.model.StudyModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by D.Knoll on 13.03.2015.
 */
public class FileStorage {

    public static void save(StudyModel study) {

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(study.getFile());

            JAXBContext jc = JAXBContext.newInstance(SystemModel.class);

            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(study.getSystemModel(), fos);


        } catch (JAXBException | FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {

                    fos.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    public static SystemModel open(String fileName) {
        FileInputStream inp = null;
        try {
            inp = new FileInputStream(fileName);
            JAXBContext ct = JAXBContext.newInstance(SystemModel.class);

            Unmarshaller u = ct.createUnmarshaller();
            SystemModel sysMod = (SystemModel)u.unmarshal(inp);
            return sysMod;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } finally {
            if (inp != null) {
                try {

                   inp.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return null;
    }
}
