package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.structure.model.ElementModel;
import ru.skoltech.cedl.dataexchange.structure.model.InstrumentModel;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by D.Knoll on 24.04.2015.
 */
public class RemoteStorage {

    public static SystemModel load(InputStream inputStream) throws IOException {
        try (InputStream inp = inputStream) {
            JAXBContext ct = JAXBContext.newInstance(SystemModel.class, SubSystemModel.class, ElementModel.class, InstrumentModel.class);

            Unmarshaller u = ct.createUnmarshaller();
            SystemModel systemModel = (SystemModel) u.unmarshal(inp);
            return systemModel;
        } catch (JAXBException e) {
            throw new IOException("Error reading system model from stream.", e);
        }
    }
}
