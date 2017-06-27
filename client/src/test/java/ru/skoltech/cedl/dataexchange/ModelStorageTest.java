package ru.skoltech.cedl.dataexchange;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.structure.SimpleSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by dknoll on 23/05/15.
 */
public class ModelStorageTest {

    private DatabaseStorage databaseStorage;

    @After
    public void cleanup() {
        databaseStorage.close();
    }

    @Test
    public void compareStoredAndRetrievedModel() throws RepositoryException {
        SystemModel systemModel = SimpleSpaceSystemBuilder.getSystemModel(2);
        System.out.println(systemModel);
        databaseStorage.storeSystemModel(systemModel);
        long systemModelId = systemModel.getId();

        SystemModel systemModel1 = databaseStorage.loadSystemModel(systemModelId);
        System.out.println(systemModel1);

        Assert.assertEquals(systemModel1.getName(), systemModel.getName());
        Assert.assertArrayEquals(systemModel1.getParameters().toArray(), systemModel.getParameters().toArray());
        Assert.assertEquals(systemModel1, systemModel);

    }

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        databaseStorage = RepositoryFactory.getTempRepository();
    }

    @Test
    public void storeModifyAndStore() throws RepositoryException {
        SystemModel generatedModel = SimpleSpaceSystemBuilder.getSystemModel(1);
        System.out.println(generatedModel);

        SystemModel storedModel = databaseStorage.storeSystemModel(generatedModel);
        long systemModelId = storedModel.getId();

        Assert.assertEquals(generatedModel, storedModel);

        ParameterModel parameterModel = storedModel.getParameters().get(0);
        String parameterName = parameterModel.getName();
        double initial = parameterModel.getValue();
        int newValue = parameterModel.getValue().intValue() * 123;
        parameterModel.setValue((double) newValue);

        SystemModel storedModel1 = databaseStorage.storeSystemModel(storedModel);
        Assert.assertEquals(storedModel, storedModel1);

        SystemModel retrievedModel = databaseStorage.loadSystemModel(systemModelId);
        ParameterModel parameterModel1 = retrievedModel.getParameterMap().get(parameterName);
        int storedValue = parameterModel1.getValue().intValue();

        System.out.println(parameterModel.toString());
        System.out.println(parameterModel1.toString());
        Assert.assertEquals(parameterModel.getId(), parameterModel1.getId());
        Assert.assertEquals(parameterModel.getName(), parameterModel1.getName());
        Assert.assertEquals("initial: " + initial, newValue, storedValue);
    }

    @Test
    public void storeModifyAndStore2() throws RepositoryException {
        SystemModel storedModel = SimpleSpaceSystemBuilder.getSystemModel(2);
        System.out.println(storedModel);

        SystemModel system0 = databaseStorage.storeSystemModel(storedModel);
        long systemModelId = storedModel.getId();

        ParameterModel parameterModel = storedModel.getParameters().get(0);
        String parameterName = parameterModel.getName();
        int newValue = parameterModel.getValue().intValue() * 123;
        parameterModel.setValue((double) newValue);

        SystemModel system1 = databaseStorage.storeSystemModel(storedModel);
        Assert.assertEquals(system0, system1);

        SystemModel retrievedModel = databaseStorage.loadSystemModel(systemModelId);
        ParameterModel parameterModel1 = retrievedModel.getParameterMap().get(parameterName);
        int storedValue = parameterModel1.getValue().intValue();

        Assert.assertEquals(newValue, storedValue);

        SystemModel retrievedModel2 = databaseStorage.loadSystemModel(systemModelId);
        ParameterModel parameterModel2 = retrievedModel2.getParameterMap().get(parameterName);
        newValue = parameterModel2.getValue().intValue() * 123;
        parameterModel.setValue((double) newValue);
        SystemModel system2 = databaseStorage.storeSystemModel(retrievedModel2);

        Assert.assertEquals(system2, retrievedModel2);
    }

    @Test
    public void storeModifyAndStoreNames() throws RepositoryException {
        SystemModel storedModel = SimpleSpaceSystemBuilder.getSystemModel(2);
        System.out.println(storedModel);

        databaseStorage.storeSystemModel(storedModel);
        long systemModelId = storedModel.getId();

        ParameterModel parameterModel = storedModel.getParameters().get(0);
        String newName = parameterModel.getName() + "1";
        parameterModel.setName(newName);
        int newValue = parameterModel.getValue().intValue() * 123;
        parameterModel.setValue((double) newValue);

        databaseStorage.storeSystemModel(storedModel);

        SystemModel retrievedModel = databaseStorage.loadSystemModel(systemModelId);
        ParameterModel parameterModel1 = retrievedModel.getParameterMap().get(newName);
        int storedValue = parameterModel1.getValue().intValue();

        Assert.assertEquals(newValue, storedValue);
        Assert.assertEquals(parameterModel, parameterModel1);
    }

    @Test
    public void testTimeStamping() throws RepositoryException {
        SystemModel systemModel = SimpleSpaceSystemBuilder.getSystemModel(1);
        System.out.println(systemModel);
        System.out.println("----------------------------------------------------------------");

        systemModel = databaseStorage.storeSystemModel(systemModel);
        long systemModelId = systemModel.getId();
        System.out.println(systemModel);
        System.out.println("----------------------------------------------------------------");

        SystemModel systemModel1 = databaseStorage.loadSystemModel(systemModelId);
        System.out.println(systemModel1);

        Assert.assertEquals(systemModel1.getName(), systemModel.getName());

        Long lastModification = systemModel.getLastModification();
        System.out.println("systemModel.lastModification: " + lastModification);
        Long lastModification1 = systemModel1.getLastModification();
        System.out.println("systemModel1.lastModification: " + lastModification1);
        Assert.assertTrue(lastModification <= lastModification1); // TODO: fix to strictly smaller!
    }
}
