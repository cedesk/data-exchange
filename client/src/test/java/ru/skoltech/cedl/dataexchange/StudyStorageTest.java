package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.services.UserManagementService;
import ru.skoltech.cedl.dataexchange.structure.BasicSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.StudySettings;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.util.List;

/**
 * Created by dknoll on 23/05/15.
 */
public class StudyStorageTest extends AbstractDatabaseTest {

    private UserManagementService userManagementService;

    @Before
    public void prepare() {
        userManagementService = context.getBean(UserManagementService.class);
    }

    @Test
    public void testStoreAndListStudies() throws RepositoryException {
        String name1 = "testStudy-1";
        Study study1 = makeStudy(name1, 1);
        repositoryService.storeStudy(study1);

        String name2 = "testStudy-2";
        Study study2 = makeStudy(name2, 1);
        repositoryService.storeStudy(study2);

        String name3 = "testStudy-3";
        Study study3 = makeStudy(name3, 1);
        repositoryService.storeStudy(study3);

        String[] createdStudies = new String[]{name1, name2, name3};
        List<String> storedStudies = repositoryService.listStudies();

        Assert.assertArrayEquals(createdStudies, storedStudies.toArray());
    }

    @Test
    public void testStoreAndRetrieveStudy() throws RepositoryException {
        String name = "testStudy";
        Study study = makeStudy(name, 2);
        System.out.println(study);

        Study study0 = repositoryService.storeStudy(study);

        Study study1 = repositoryService.loadStudy(name);
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

    private Study makeStudy(String projectName, int modelDepth) {
        Study study = new Study();
        study.setStudySettings(new StudySettings());
        SystemModel systemModel = BasicSpaceSystemBuilder.getSystemModel(modelDepth);
        study.setSystemModel(systemModel);
        study.setName(projectName);
        UserRoleManagement userRoleManagement = userManagementService.createUserRoleManagementWithSubsystemDisciplines(systemModel, null);
        study.setUserRoleManagement(userRoleManagement);
        return study;
    }
}
