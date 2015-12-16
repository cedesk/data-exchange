package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.logging.LogEntry;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.io.Closeable;
import java.util.List;

/**
 * Created by D.Knoll on 25.05.2015.
 */
public interface Repository extends Closeable {

    boolean validateDatabaseScheme();

    boolean updateDatabaseScheme();

    String getUrl();

    String getSchema();

    List<String> listStudies() throws RepositoryException;

    Study loadStudy(String name) throws RepositoryException;

    void deleteStudy(String name) throws RepositoryException;

    Study storeStudy(Study study) throws RepositoryException;

    SystemModel loadSystemModel(long studyId) throws RepositoryException;

    SystemModel storeSystemModel(SystemModel systemModel) throws RepositoryException;

    ExternalModel loadExternalModel(long externalModelId) throws RepositoryException;

    ExternalModel storeExternalModel(ExternalModel externalModel) throws RepositoryException;

    UserRoleManagement loadUserRoleManagement(long studyId) throws RepositoryException;

    UserRoleManagement storeUserRoleManagement(UserRoleManagement userRoleManagement) throws RepositoryException;

    UserManagement loadUserManagement() throws RepositoryException;

    UserManagement storeUserManagement(UserManagement userManagement) throws RepositoryException;

    UnitManagement loadUnitManagement() throws RepositoryException;

    UnitManagement storeUnitManagement(UnitManagement unitManagement) throws RepositoryException;

    List<ParameterRevision> getChangeHistory(ParameterModel parameterModel) throws RepositoryException;

    void storeLog(LogEntry logEntry);

    Long getLastStudyModification(String name);
}
