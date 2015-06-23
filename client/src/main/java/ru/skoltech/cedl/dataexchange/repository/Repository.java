package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterRevision;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.util.List;

/**
 * Created by D.Knoll on 25.05.2015.
 */
public interface Repository {

    SystemModel loadSystemModel(long studyId) throws RepositoryException;

    UserRoleManagement loadUserRoleManagement(long studyId) throws RepositoryException;

    SystemModel storeSystemModel(SystemModel systemModel) throws RepositoryException;

    void close();

    Study loadStudy(String name) throws RepositoryException;

    Study storeStudy(Study study) throws RepositoryException;

    UserRoleManagement storeUserRoleManagement(UserRoleManagement userRoleManagement) throws RepositoryException;

    UserManagement storeUserManagement(UserManagement userManagement) throws RepositoryException;

    UserManagement loadUserManagement() throws RepositoryException;

    List<ParameterRevision> getChangeHistory(ParameterModel parameterModel);

    String getUrl();
}
