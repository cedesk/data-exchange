package ru.skoltech.cedl.dataexchange.structure.model;

import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.users.UserManagementFactory;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Created by dknoll on 25/05/15.
 */
public class StudyFactory {

    public static Study makeStudy(String projectName, UserManagement userManagement) {
        return makeStudy(projectName, 4, userManagement);
    }

    public static Study makeStudy(String projectName, int modelDepth, UserManagement userManagement) {
        Study study = new Study();
        study.setStudySettings(new StudySettings());
        study.setSystemModel(DummySystemBuilder.getSystemModel(modelDepth));
        study.setName(projectName);
        addUserRoleManagement(study, userManagement);
        return study;
    }

    private static void addUserRoleManagement(Study study, UserManagement userManagement) {
        UserRoleManagement userRoleManagement = UserManagementFactory.getUserRoleManagement(userManagement);
        study.setUserRoleManagement(userRoleManagement);
    }
}
