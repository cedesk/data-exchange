package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Created by D.Knoll on 25.05.2015.
 */
public interface Repository {

    SystemModel loadSystemModel(long studyId) throws RepositoryException;

    UserRoleManagement loadUserRoleManagement(long studyId) throws RepositoryException;

    void storeSystemModel(SystemModel systemModel) throws RepositoryException;

    void close();

    Study loadStudy(String name) throws RepositoryException;

    void storeStudy(Study study);

    void storeUserRoleManagement(UserRoleManagement userRoleManagement) throws RepositoryException;

    void storeUserManagement(UserManagement userManagement) throws RepositoryException;

    UserManagement loadUserManagement() throws RepositoryException;

    String getUrl();
}
