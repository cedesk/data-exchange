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
import ru.skoltech.cedl.dataexchange.structure.model.ParameterRevision;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by D.Knoll on 23.06.2015.
 */
public class VersioningStorageTest {

    public static final String ADMIN = "admin";
    private DatabaseStorage databaseStorage;

    @After
    public void cleanup() {
        databaseStorage.close();
    }

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        databaseStorage = RepositoryFactory.getTempRepository();

        ApplicationSettings.setUseOsUser(false);
        ApplicationSettings.setProjectUser(ADMIN);
    }

    @Test
    public void test() throws RepositoryException {
        SystemModel systemModel = SimpleSpaceSystemBuilder.getSystemModel(1);
        System.out.println(systemModel);
        databaseStorage.storeSystemModel(systemModel);

        ParameterModel parameterModel = systemModel.getParameters().get(0);
        parameterModel.setName("parameter-1-renamed");

        //ParameterModel newParameterModel = new ParameterModel("new-parameter-A", 3.1415);
        //systemModel.addParameter(newParameterModel);

        databaseStorage.storeSystemModel(systemModel);

        List<ParameterRevision> changeHistory = databaseStorage.getChangeHistory(parameterModel);

        Assert.assertEquals(2, changeHistory.size());

        Assert.assertEquals(ADMIN, changeHistory.get(0).getRevisionAuthor());
    }
}
