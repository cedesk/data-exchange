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
import java.util.List;

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
        Study study = StudyFactory.makeStudy(name, 2, null);
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

    @Test
    public void storeAndListStudies() throws RepositoryException {
        String name1 = "testStudy-1";
        Study study1 = StudyFactory.makeStudy(name1, 1, null);
        databaseStorage.storeStudy(study1);

        String name2 = "testStudy-2";
        Study study2 = StudyFactory.makeStudy(name2, 1, null);
        databaseStorage.storeStudy(study2);

        String name3 = "testStudy-3";
        Study study3 = StudyFactory.makeStudy(name3, 1, null);
        databaseStorage.storeStudy(study3);

        String[] createdStudies = new String[]{name1, name2, name3};
        List<String> storedStudies = databaseStorage.listStudies();

        Assert.assertArrayEquals(createdStudies, storedStudies.toArray());
    }
}
