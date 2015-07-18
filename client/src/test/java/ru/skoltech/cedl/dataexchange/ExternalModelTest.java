package ru.skoltech.cedl.dataexchange;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

/**
 * Created by D.Knoll on 02.07.2015.
 */
public class ExternalModelTest {

    private Repository repository;

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        repository = RepositoryFactory.getTempRepository();
    }

    @After
    public void cleanup() {
        try {
            repository.close();
        } catch (IOException ignore) {
        }
    }

    @Test()
    public void storeAndRetrieveAttachment() throws URISyntaxException, IOException, RepositoryException {
        File file = new File(this.getClass().getResource("/attachment.xls").toURI());

        SystemModel testSat = new SystemModel("testSat");
        repository.storeSystemModel(testSat);

        ExternalModel externalModel = ExternalModelFileHandler.newFromFile(file, testSat);

        System.err.println("before: " + externalModel.getId());
        ExternalModel externalModel1 = repository.storeExternalModel(externalModel);
        long pk = externalModel.getId();
        System.err.println("after: " + pk);
        System.err.println("second: " + externalModel1.getId());

        ExternalModel externalModel2 = repository.loadExternalModel(pk);

        Assert.assertArrayEquals(externalModel1.getAttachment(), externalModel2.getAttachment());
    }

    @Test
    public void testSetLastModified() throws IOException {
        long time = 1316137362000L;
        File file = new File("foo.test");
        file.createNewFile();
        file.setLastModified(time);
        Assert.assertEquals(time, file.lastModified());
    }
}