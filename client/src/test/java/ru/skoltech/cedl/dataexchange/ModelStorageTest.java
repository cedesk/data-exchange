package ru.skoltech.cedl.dataexchange;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.repository.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by dknoll on 23/05/15.
 */
public class ModelStorageTest {

    private DatabaseStorage databaseStorage;

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
        Assert.assertArrayEquals(systemModel1.getParameters().toArray(), systemModel.getParameters().toArray());
        Assert.assertEquals(systemModel1, systemModel);
    }

    @Test
    public void storeModifyAndStore() throws RepositoryException {
        SystemModel storedModel = DummySystemBuilder.getSystemModel(1);
        System.out.println(storedModel);

        databaseStorage.storeSystemModel(storedModel);
        long systemModelId = storedModel.getId();

        ParameterModel parameterModel = storedModel.getParameters().get(0);
        int newValue = parameterModel.getValue().intValue() * 123;
        parameterModel.setValue((double) newValue);

        databaseStorage.storeSystemModel(storedModel);

        SystemModel retrievedModel = databaseStorage.loadSystemModel(systemModelId);
        ParameterModel parameterModel1 = retrievedModel.getParameters().get(0);
        int storedValue = parameterModel1.getValue().intValue();

        Assert.assertEquals(newValue, storedValue);
    }

    @Test
    public void storeModifyAndStore2() throws RepositoryException {
        SystemModel storedModel = DummySystemBuilder.getSystemModel(2);
        System.out.println(storedModel);

        SystemModel system0 = databaseStorage.storeSystemModel(storedModel);
        long systemModelId = storedModel.getId();

        ParameterModel parameterModel = storedModel.getParameters().get(0);
        int newValue = parameterModel.getValue().intValue() * 123;
        parameterModel.setValue((double) newValue);

        SystemModel system1 = databaseStorage.storeSystemModel(storedModel);
        Assert.assertEquals(system0, system1);

        SystemModel retrievedModel = databaseStorage.loadSystemModel(systemModelId);
        ParameterModel parameterModel1 = retrievedModel.getParameters().get(0);
        int storedValue = parameterModel1.getValue().intValue();

        Assert.assertEquals(newValue, storedValue);

        SystemModel retrievedModel2 = databaseStorage.loadSystemModel(systemModelId);
        ParameterModel parameterModel2 = retrievedModel2.getParameters().get(0);
        newValue = parameterModel2.getValue().intValue() * 123;
        parameterModel.setValue((double) newValue);
        SystemModel system2 = databaseStorage.storeSystemModel(retrievedModel2);

        Assert.assertEquals(system2, retrievedModel2);
    }

    @Test
    public void storeModifyAndStoreNames() throws RepositoryException {
        SystemModel storedModel = DummySystemBuilder.getSystemModel(2);
        System.out.println(storedModel);

        databaseStorage.storeSystemModel(storedModel);
        long systemModelId = storedModel.getId();

        ParameterModel parameterModel = storedModel.getParameters().get(0);
        String newName = parameterModel.getName() + "1";
        parameterModel.setName(newName);
        int newValue = (int) (parameterModel.getValue().intValue() * 123);
        parameterModel.setValue((double) newValue);

        databaseStorage.storeSystemModel(storedModel);

        SystemModel retrievedModel = databaseStorage.loadSystemModel(systemModelId);
        ParameterModel parameterModel1 = retrievedModel.getParameters().get(0);
        String retName = parameterModel1.getName();
        int storedValue = parameterModel1.getValue().intValue();

        Assert.assertEquals(newName, retName);
        Assert.assertEquals(newValue, storedValue);
    }
}
