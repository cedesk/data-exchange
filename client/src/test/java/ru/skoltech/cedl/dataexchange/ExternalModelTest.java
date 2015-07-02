package ru.skoltech.cedl.dataexchange;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by D.Knoll on 02.07.2015.
 */
public class ExternalModelTest {

    private DatabaseStorage databaseStorage;

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        databaseStorage = RepositoryFactory.getTempRepository();
    }

    @After
    public void cleanup() {
        databaseStorage.close();
    }

    @Test
    public void storeAndRetrieveAttachment() throws URISyntaxException, IOException, RepositoryException {
        URL url = this.getClass().getResource("/attachment.xls");
        File file = new File(url.toURI());
        Path path = Paths.get(file.getAbsolutePath());
        byte[] data = Files.readAllBytes(path);
        ExternalModel externalModel = new ExternalModel();
        externalModel.setAttachment(data);
        databaseStorage.storeExternalModel(externalModel);
    }
}