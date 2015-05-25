package ru.skoltech.cedl.dataexchange.structure.model;

import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.users.DummyUserManagementBuilder;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

/**
 * Created by dknoll on 25/05/15.
 */
public class StudyFactory {

    public static Study makeStudy(String projectName) {
        Study study = new Study(projectName);
        study.setUserManagement(getEmptyUserManagement());
        study.setSystemModel(new SystemModel(projectName));
        return study;
    }

    public static Study makeStudy(String projectName, int modelDepth) {
        Study study = new Study(projectName);
        study.setUserManagement(getEmptyUserManagement());
        study.setSystemModel(DummySystemBuilder.getSystemModel(modelDepth));
        return study;
    }

    private static UserManagement getEmptyUserManagement() {
        // TODO: remove dummy, make empty
        return DummyUserManagementBuilder.getModel();
    }

}
