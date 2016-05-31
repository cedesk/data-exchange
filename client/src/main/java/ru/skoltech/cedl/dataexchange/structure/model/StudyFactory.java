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
        SystemModel systemModel = DummySystemBuilder.getSystemModel(modelDepth);
        study.setSystemModel(systemModel);
        study.setName(projectName);
        UserRoleManagement userRoleManagement = UserManagementFactory.makeUserRoleManagementWithSubsystemDisciplines(systemModel, userManagement);
        study.setUserRoleManagement(userRoleManagement);
        return study;
    }
}
