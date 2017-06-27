package ru.skoltech.cedl.dataexchange;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.structure.SimpleSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.StudySettings;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.UserManagementFactory;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Created by dknoll on 23/05/15.
 */
public class StudyStorageTest {

    private DatabaseStorage databaseStorage;

    private static Study makeStudy(String projectName, int modelDepth) {
        Study study = new Study();
        study.setStudySettings(new StudySettings());
        SystemModel systemModel = SimpleSpaceSystemBuilder.getSystemModel(modelDepth);
        study.setSystemModel(systemModel);
        study.setName(projectName);
        UserRoleManagement userRoleManagement = UserManagementFactory.makeUserRoleManagementWithSubsystemDisciplines(systemModel, null);
        study.setUserRoleManagement(userRoleManagement);
        return study;
    }

    @After
    public void cleanup() {
        databaseStorage.close();
    }

    @Before
    public void prepare() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        databaseStorage = RepositoryFactory.getTempRepository();
    }

    @Test
    public void storeAndListStudies() throws RepositoryException {
        String name1 = "testStudy-1";
        Study study1 = makeStudy(name1, 1);
        databaseStorage.storeStudy(study1);

        String name2 = "testStudy-2";
        Study study2 = makeStudy(name2, 1);
        databaseStorage.storeStudy(study2);

        String name3 = "testStudy-3";
        Study study3 = makeStudy(name3, 1);
        databaseStorage.storeStudy(study3);

        String[] createdStudies = new String[]{name1, name2, name3};
        List<String> storedStudies = databaseStorage.listStudies();

        Assert.assertArrayEquals(createdStudies, storedStudies.toArray());
    }

    @Test
    public void storeAndRetrieveStudy() throws RepositoryException {
        String name = "testStudy";
        Study study = makeStudy(name, 2);
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
}
