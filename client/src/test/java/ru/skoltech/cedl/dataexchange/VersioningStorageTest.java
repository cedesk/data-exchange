package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.structure.BasicSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterRevision;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by D.Knoll on 23.06.2015.
 */
public class VersioningStorageTest extends AbstractDatabaseTest {

    public static final String ADMIN = "admin";

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        ApplicationSettings applicationSettings = context.getBean(ApplicationSettings.class);
        applicationSettings.setUseOsUser(false);
    }

    @Test
    public void test() throws RepositoryException {
        SystemModel systemModel = BasicSpaceSystemBuilder.getSystemModel(1);
        System.out.println(systemModel);
        repositoryService.storeSystemModel(systemModel);

        ParameterModel parameterModel = systemModel.getParameters().get(0);
        parameterModel.setName("parameter-1-renamed");

        //ParameterModel newParameterModel = new ParameterModel("new-parameter-A", 3.1415);
        //systemModel.addParameter(newParameterModel);

        repositoryService.storeSystemModel(systemModel);

        List<ParameterRevision> changeHistory = repositoryService.getChangeHistory(parameterModel);

        Assert.assertEquals(2, changeHistory.size());

        Assert.assertEquals(ADMIN, changeHistory.get(0).getRevisionAuthor());
    }
}
