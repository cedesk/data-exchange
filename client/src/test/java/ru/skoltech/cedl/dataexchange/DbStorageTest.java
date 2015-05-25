package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.repository.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by dknoll on 23/05/15.
 */
public class DbStorageTest {

    private SystemModel systemModel;

    private DatabaseStorage databaseStorage;

    @Before
    public void storeAndRetrieve() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        systemModel = DummySystemBuilder.getSystemModel(4);
        System.out.println(systemModel);
        Constructor<DatabaseStorage> constructor = DatabaseStorage.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        databaseStorage = constructor.newInstance();
        databaseStorage.storeSystemModel(systemModel);
    }

    @Test
    public void compareStoredAndRetrievedModel() {
        ModelNode modelNode = databaseStorage.loadSystemModel();
        System.out.println(modelNode);

        Assert.assertEquals(modelNode.getName(), systemModel.getName());

        Assert.assertEquals(modelNode.getParameters(), systemModel.getParameters());

        Assert.assertEquals(modelNode, systemModel);

    }
}
