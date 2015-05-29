package ru.skoltech.cedl.dataexchange;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.repository.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by dknoll on 23/05/15.
 */
public class ModelStorageTest {

    private static DatabaseStorage databaseStorage;

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<DatabaseStorage> constructor = DatabaseStorage.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        databaseStorage = constructor.newInstance();
    }

    @After
    public void cleanup() {
        databaseStorage.close();
    }

    @Test
    public void compareStoredAndRetrievedModel() throws RepositoryException {
        SystemModel systemModel = DummySystemBuilder.getSystemModel(4);
        System.out.println(systemModel);
        databaseStorage.storeSystemModel(systemModel);
        long systemModelId = systemModel.getId();

        databaseStorage.storeSystemModel(DummySystemBuilder.getSystemModel(1));

        SystemModel systemModel1 = databaseStorage.loadSystemModel(systemModelId);
        System.out.println(systemModel1);

        Assert.assertEquals(systemModel1.getName(), systemModel.getName());
        Assert.assertEquals(systemModel1.getParameters(), systemModel.getParameters());
        Assert.assertEquals(systemModel1, systemModel);
    }

}
