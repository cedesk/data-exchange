package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.ApplicationProperties;
import ru.skoltech.cedl.dataexchange.db.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.logging.LogEntry;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import javax.transaction.Transactional;
import java.io.Closeable;
import java.util.List;

/**
 * Created by D.Knoll on 25.05.2015.
 */
public interface Repository extends Closeable {

    public static final String PERSISTENCE_URL_PROPERTY = "javax.persistence.jdbc.url";
    public static final String PERSISTENCE_USER_PROPERTY = "javax.persistence.jdbc.user";
    public static final String PERSISTENCE_PASSWORD_PROPERTY = "javax.persistence.jdbc.password";
    public static final String HIBERNATE_TABLE_MAPPING = "hibernate.hbm2ddl.auto";
    public static final String HIBERNATE_TABLE_MAPPING_UPDATE = "update";
    public static final String DEFAULT_HOST_NAME = ApplicationProperties.getDefaultRepositoryHost();
    public static final String DEFAULT_SCHEMA = "cedesk_repo";
    public static final String DEFAULT_USER_NAME = "cedesk";
    public static final String DEFAULT_PASSWORD = "cedesk";
    public static final String DB_PERSISTENCE_UNIT_NAME = "db";
    public static final String MEM_PERSISTENCE_UNIT_NAME = "mem";


    boolean validateDatabaseScheme();

    @Transactional
    boolean updateDatabaseScheme();

    List<String> listStudies() throws RepositoryException;

    Study loadStudy(String name) throws RepositoryException;

    @Transactional
    void deleteStudy(String name) throws RepositoryException;

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
}
