package ru.skoltech.cedl.dataexchange;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.ExternalModelUtil;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

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

    @Test()
    public void storeAndRetrieveAttachment() throws URISyntaxException, IOException, RepositoryException {
        File file = new File(this.getClass().getResource("/attachment.xls").toURI());

//        SystemModel testSat = new SystemModel("testSat");
//        databaseStorage.storeSystemModel(testSat);

        ExternalModel externalModel = ExternalModelUtil.fromFile(file, null);

        System.err.println("before: " + externalModel.getId());
        ExternalModel externalModel1 = databaseStorage.storeExternalModel(externalModel);
        long pk = externalModel.getId();
        System.err.println("after: " + pk);
        System.err.println("second: " + externalModel1.getId());

        ExternalModel externalModel2 = databaseStorage.loadExternalModel(pk);

        Assert.assertArrayEquals(externalModel1.getAttachment(), externalModel2.getAttachment());
    }
}