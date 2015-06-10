package ru.skoltech.cedl.dataexchange.structure.model;

import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.users.UserManagementFactory;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Created by dknoll on 25/05/15.
 */
public class StudyFactory {

    public static Study makeStudy(String projectName) {
        Study study = new Study(projectName);
        study.setSystemModel(new SystemModel(projectName));
        addUserRoleManagement(study);
        return study;
    }

    public static Study makeStudy(String projectName, int modelDepth) {
        Study study = new Study(projectName);
        study.setSystemModel(DummySystemBuilder.getSystemModel(modelDepth));
        addUserRoleManagement(study);
        return study;
    }

    public static void addUserRoleManagement(Study study) {
        UserRoleManagement userRoleManagement = UserManagementFactory.getUserRoleManagement();
        study.setUserRoleManagement(userRoleManagement);
        //UserManagementFactory.addUserWithAllPower(userRoleManagement, getUserManagement(), Utils.getUserName());
    }
}
