package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.repository.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

/**
 * Created by dknoll on 23/05/15.
 */
public class DbStorageTest {

    private SystemModel systemModel;

    private DatabaseStorage databaseStorage;

    @Before
    public void storeAndRetrieve() {
        systemModel = DummySystemBuilder.getSystemModel(4);
        System.out.println(systemModel);
        databaseStorage = new DatabaseStorage();
        databaseStorage.storeModel(systemModel);
    }

    @Test
    public void compareStoredAndRetrievedModel() {
        ModelNode modelNode = databaseStorage.loadModel();
        System.out.println(modelNode);

        Assert.assertEquals(modelNode.getName(), systemModel.getName());

        Assert.assertEquals(modelNode.getParameters(), systemModel.getParameters());

        Assert.assertEquals(modelNode, systemModel);

    }
}
