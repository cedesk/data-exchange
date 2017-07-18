package ru.skoltech.cedl.dataexchange.services;

import ru.skoltech.cedl.dataexchange.db.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.logging.LogEntry;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Operation with repository.
 *
 * Created by D.Knoll on 25.05.2015.
 */
public interface RepositoryService {

    List<String> listStudies() throws RepositoryException;

    Study loadStudy(String name) throws RepositoryException;

    @Transactional
    void deleteStudy(String name) throws RepositoryException;

    @Transactional
    Study storeStudy(Study study) throws RepositoryException;

    SystemModel loadSystemModel(long studyId) throws RepositoryException;

    @Transactional
    SystemModel storeSystemModel(SystemModel systemModel) throws RepositoryException;

    ExternalModel loadExternalModel(long externalModelId) throws RepositoryException;

    @Transactional
    ExternalModel storeExternalModel(ExternalModel externalModel) throws RepositoryException;

    UserRoleManagement loadUserRoleManagement(long studyId) throws RepositoryException;

    @Transactional
    UserRoleManagement storeUserRoleManagement(UserRoleManagement userRoleManagement) throws RepositoryException;

    UserManagement loadUserManagement() throws RepositoryException;

    @Transactional
    UserManagement storeUserManagement(UserManagement userManagement) throws RepositoryException;

    UnitManagement loadUnitManagement() throws RepositoryException;

    @Transactional
    UnitManagement storeUnitManagement(UnitManagement unitManagement) throws RepositoryException;

    List<ParameterRevision> getChangeHistory(ParameterModel parameterModel) throws RepositoryException;

    CustomRevisionEntity getLastRevision(PersistedEntity persistedEntity);

    @Transactional
    void storeLog(LogEntry logEntry);

    Long getLastStudyModification(String name);

    String loadSchemeVersion()  throws RepositoryException;

    boolean storeSchemeVersion(String schemeVersion) throws RepositoryException;
}
