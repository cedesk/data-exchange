package ru.skoltech.cedl.dataexchange;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.StudyFactory;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by dknoll on 23/05/15.
 */
public class StudyStorageTest {

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
    public void storeAndRetrieveStudy() throws RepositoryException {
        String name = "testStudy";
        Study study = StudyFactory.makeStudy(name, null);
        System.out.println(study);

        Study study0 = databaseStorage.storeStudy(study);

        Study study1 = databaseStorage.loadStudy(name);
        System.out.println(study1);

        Assert.assertEquals(study.getId(), study1.getId());
        Assert.assertEquals(study.getName(), study1.getName());
        Assert.assertEquals(study.getVersion(), study1.getVersion());

        Assert.assertEquals(study.getSystemModel(), study1.getSystemModel());

        UserRoleManagement urm1 = study.getUserRoleManagement();
        UserRoleManagement urm2 = study1.getUserRoleManagement();
        Assert.assertEquals(urm1, urm2);
        Assert.assertEquals(study, study1);
    }
/*
    @Test
    public void storeModifyAndStore() throws RepositoryException {
        String name = "testStudy";
        Study study = StudyFactory.makeStudy(name, null);
        System.out.println(study);

        Study study0 = databaseStorage.storeStudy(study);

        Study study1 = databaseStorage.loadStudy(name);
        System.out.println(study1);

        Assert.assertEquals(study, study1);
    }
*/
}
