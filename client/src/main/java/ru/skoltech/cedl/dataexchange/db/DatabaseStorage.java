package ru.skoltech.cedl.dataexchange.db;

import org.apache.log4j.Logger;
import org.hibernate.StaleObjectStateException;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.logging.LogEntry;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.units.UnitManagementFactory;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;
import ru.skoltech.cedl.dataexchange.users.UserManagementFactory;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dknoll on 24/05/15.
 */
public class DatabaseStorage implements Repository {

    public static final String PERSISTENCE_URL_PROPERTY = "javax.persistence.jdbc.url";
    public static final String PERSISTENCE_USER_PROPERTY = "javax.persistence.jdbc.user";
    public static final String PERSISTENCE_PASSWORD_PROPERTY = "javax.persistence.jdbc.password";
    public static final String HIBERNATE_TABLE_MAPPING = "hibernate.hbm2ddl.auto";
    public static final String HIBERNATE_TABLE_MAPPING_UPDATE = "update";
    public static final String DEFAULT_HOST_NAME = "localhost";
    public static final String DEFAULT_SCHEMA = "cedesk_repo";
    public static final String DEFAULT_USER_NAME = "cedesk";
    public static final String DEFAULT_PASSWORD = "cedesk";
    public static final String DB_PERSISTENCE_UNIT_NAME = "db";
    public static final String MEM_PERSISTENCE_UNIT_NAME = "mem";
    private static final Logger logger = Logger.getLogger(DatabaseStorage.class);
    private static final String HOST_NAME = "HOSTNAME";
    private static final String SCHEMA = "SCHEMA";
    private static final String DEFAULT_JDBC_URL = "jdbc:mysql://" + HOST_NAME + ":3306/" + SCHEMA;
    private String hostName;
    private String schema;
    private EntityManagerFactory emf;
    private String persistenceUnit;
    private Map<String, Object> properties = new HashMap<>();

    /**
     * The default backend uses a DB on the localhost.
     */
    public DatabaseStorage(String persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
    }

    public DatabaseStorage(String persistenceUnit, String hostName, String schema, String userName, String password) {
        this.persistenceUnit = persistenceUnit;
        this.hostName = hostName;
        this.schema = schema;
        String url = DEFAULT_JDBC_URL.replace(HOST_NAME, hostName);
        url = url.replace(SCHEMA, schema);
        logger.debug("repository url: " + url + ", user: " + userName);
        properties.put(PERSISTENCE_URL_PROPERTY, url);
        properties.put(PERSISTENCE_USER_PROPERTY, userName);
        properties.put(PERSISTENCE_PASSWORD_PROPERTY, password);
    }

    public static boolean checkDatabaseConnection(String hostName, String schema, String userName, String password) {
        String url = DEFAULT_JDBC_URL.replace(HOST_NAME, hostName);
        url = url.replace(SCHEMA, schema);
        logger.debug("repository url: " + url + ", user: " + userName);
        try {
            DriverManager.getConnection(url, userName, password).close();
            logger.info("check of database connection succeeded!");
            return true;
        } catch (SQLException e) {
            logger.warn("check of database connection failed!");
            return false;
        }
    }

    @Override
    public boolean validateDatabaseScheme() {
        EntityManagerFactory entityManagerFactory = null;
        EntityManager entityManager = null;
        logger.info("validating database scheme");
        try {
            entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit, properties);
            entityManager = entityManagerFactory.createEntityManager();
        } catch (Exception e) {
            StatusLogger.getInstance().log("Database scheme validation failed!", true);
            logger.error("Database scheme validation failed!", e);
            return false;
        } finally {
            if (entityManager != null)
                try {
                    entityManager.close();
                } catch (Exception ignore) {
                }
            if (entityManagerFactory != null) {
                try {
                    entityManagerFactory.close();
                } catch (Exception ignore) {
                }
            }
        }
        ApplicationProperty targetSchemaVersion = ApplicationProperty.DB_SCHEMA_VERSION;
        ApplicationProperty actualSchemaVersion = null;
        try {
            actualSchemaVersion = loadApplicationProperty(targetSchemaVersion);
        } catch (RepositoryException e) {
            logger.debug("error loading the applications version property", e);
        }
        if (actualSchemaVersion == null) {
            logger.error("No DB Schema Version!");
            return false;
        }
        int versionCompare = Utils.compareVersions(actualSchemaVersion.getValue(), targetSchemaVersion.getValue());
        if (versionCompare == 0) {
            return true;
        } else if (versionCompare < 0) {
            StatusLogger.getInstance().log("Upgrade your CEDESK Client! Current Application Version requires a DB Schema Version " + targetSchemaVersion.getValue()
                    + ", which is incompatible with current DB Schema Version " + actualSchemaVersion.getValue());
            return false;
        } else if (versionCompare > 0) {
            StatusLogger.getInstance().log("Have the administrator upgrade the DB Schema! Current Application Version requires a DB Schema Version " + targetSchemaVersion.getValue()
                    + ", which is incompatible with current DB Schema Version " + actualSchemaVersion.getValue());
            return false;
        }
        return false;
    }

    @Override
    public boolean updateDatabaseScheme() {
        EntityManagerFactory entityManagerFactory = null;
        EntityManager entityManager = null;
        logger.info("updating database scheme");
        try {
            properties.put(HIBERNATE_TABLE_MAPPING, HIBERNATE_TABLE_MAPPING_UPDATE);
            entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit, properties);
            entityManager = entityManagerFactory.createEntityManager();
            properties.remove(HIBERNATE_TABLE_MAPPING);
        } catch (Exception e) {
            StatusLogger.getInstance().log("Database scheme update failed!", true);
            logger.error("Database scheme update failed!", e);
        } finally {
            if (entityManager != null) {
                try {
                    entityManager.close();
                } catch (Exception ignore) {
                }
            }
            if (entityManagerFactory != null) {
                try {
                    entityManagerFactory.close();
                } catch (Exception ignore) {
                }
            }
        }
        ApplicationProperty targetSchemaVersion = ApplicationProperty.DB_SCHEMA_VERSION;
        ApplicationProperty actualSchemaVersion = null;
        try {
            actualSchemaVersion = loadApplicationProperty(targetSchemaVersion);
        } catch (RepositoryException e) {
            logger.debug("error loading the applications version property", e);
        }
        if (actualSchemaVersion == null) {
            try {
                return storeApplicationProperty(targetSchemaVersion);
            } catch (RepositoryException e) {
                logger.debug("error storing the applications version property", e);
            }
        } else {
            int versionCompare = Utils.compareVersions(actualSchemaVersion.getValue(), targetSchemaVersion.getValue());
            if (versionCompare <= 0) {
                try {
                    return storeApplicationProperty(targetSchemaVersion);
                } catch (RepositoryException e) {
                    logger.debug("error storing the applications version property", e);
                }
            } else {
                StatusLogger.getInstance().log("Downgrade your CEDESK Client! Current Application Version " + targetSchemaVersion.getValue()
                        + ", is older than current DB Schema Version " + actualSchemaVersion.getValue());
                return false;
            }
        }
        return false;
    }

    private boolean storeApplicationProperty(ApplicationProperty applicationProperty) throws RepositoryException {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            entityManager.setFlushMode(FlushModeType.AUTO);
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            ApplicationProperty appProp = entityManager.find(ApplicationProperty.class, applicationProperty.getId());
            if (appProp == null) {
                entityManager.persist(applicationProperty);
            } else {
                entityManager.merge(applicationProperty);
            }
            transaction.commit();
            return true;
        } catch (Exception e) {
            throw new RepositoryException("Storing ApplicationProperty failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
    }

    private ApplicationProperty loadApplicationProperty(ApplicationProperty applicationProperty) throws RepositoryException {/*
        EntityManager entityManager = null;
        ApplicationProperty appProp = null;
        try {
            entityManager = getEntityManager();
            appProp = entityManager.find(ApplicationProperty.class, applicationProperty.getId());
        } catch (Exception e) {
            throw new RepositoryException("Loading ApplicationProperty failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
        return appProp;*/
        return applicationProperty;
    }

    @Override
    public Study storeStudy(Study study) throws RepositoryException {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            entityManager.setFlushMode(FlushModeType.AUTO);
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            if (study.getId() == 0) {
                entityManager.persist(study);
            } else {
                study = entityManager.merge(study);
            }
            long latestModification = study.getSystemModel().findLatestModification();
            study.setLatestModelModification(latestModification);
            study = entityManager.merge(study);
            transaction.commit();
        } catch (OptimisticLockException | RollbackException re) {
            logger.warn("transaction failed", re);
            throw extractAndRepackCause(re);
        } catch (Exception e) {
            throw new RepositoryException("Storing Study failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
        return study;
    }

    @Override
    public void deleteStudy(String name) throws RepositoryException {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            final CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(Study.class);
            final Root studyRoot = criteriaQuery.from(Study.class);
            final Predicate namePredicate = criteriaBuilder.equal(studyRoot.get("name"), name);
            criteriaQuery.where(namePredicate);
            final TypedQuery query = entityManager.createQuery(criteriaQuery);
            Object singleResult = query.getSingleResult();
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.remove(singleResult);
            transaction.commit();
        } catch (Exception e) {
            throw new RepositoryException("Deleting Study failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public List<String> listStudies() throws RepositoryException {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<String> criteria = builder.createQuery(String.class);
            Root<Study> personRoot = criteria.from(Study.class);
            criteria.select(personRoot.get("name"));
            List<String> studyNamesList = entityManager.createQuery(criteria).getResultList();
            return studyNamesList;
        } catch (Exception e) {
            throw new RepositoryException("Study loading failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public Study loadStudy(String name) throws RepositoryException {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            final CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(Study.class);
            final Root studyRoot = criteriaQuery.from(Study.class);
            final Predicate namePredicate = criteriaBuilder.equal(studyRoot.get("name"), name);
            criteriaQuery.where(namePredicate);
            final TypedQuery query = entityManager.createQuery(criteriaQuery);
            Object singleResult = query.getSingleResult();
            //entityManager.refresh(singleResult);
            return (Study) singleResult;
        } catch (NoResultException e) {
            throw new RepositoryException("Study not found.", e);
        } catch (Exception e) {
            throw new RepositoryException("Study loading failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public UserRoleManagement storeUserRoleManagement(UserRoleManagement userRoleManagement) throws
            RepositoryException {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            if (userRoleManagement.getId() == 0) {
                entityManager.persist(userRoleManagement);
            } else {
                userRoleManagement = entityManager.merge(userRoleManagement);
            }
            transaction.commit();
        } catch (Exception e) {
            throw new RepositoryException("Storing UserRoleManagement failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
        return userRoleManagement;
    }

    @Override
    public UserManagement storeUserManagement(UserManagement userManagement) throws RepositoryException {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            if (userManagement.getId() == 0) {
                entityManager.persist(userManagement);
            } else {
                userManagement = entityManager.merge(userManagement);
            }
            transaction.commit();
        } catch (Exception e) {
            throw new RepositoryException("Storing UserManagement failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
        return userManagement;
    }

    @Override
    public UserRoleManagement loadUserRoleManagement(long studyId) throws RepositoryException {
        EntityManager entityManager = getEntityManager();
        UserRoleManagement userRoleManagement = null;
        try {
            userRoleManagement = entityManager.find(UserRoleManagement.class, studyId);
        } catch (Exception e) {
            throw new RepositoryException("Loading UserRoleManagement failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
        if (userRoleManagement == null)
            throw new RepositoryException("UserRoleManagement not found.");

        return userRoleManagement;
    }

    @Override
    public UnitManagement storeUnitManagement(UnitManagement unitManagement) throws RepositoryException {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            if (unitManagement.getId() == 0) {
                entityManager.persist(unitManagement);
            } else {
                unitManagement = entityManager.merge(unitManagement);
            }
            transaction.commit();
        } catch (Exception e) {
            throw new RepositoryException("Storing UnitManagement failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
        return unitManagement;
    }

    @Override
    public UnitManagement loadUnitManagement() throws RepositoryException {
        EntityManager entityManager = getEntityManager();
        UnitManagement unitManagement = null;
        try {
            unitManagement = entityManager.find(UnitManagement.class, UnitManagementFactory.IDENTIFIER);
        } catch (Exception e) {
            throw new RepositoryException("Loading UnitManagement failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
        if (unitManagement == null)
            throw new RepositoryException("UnitManagement not found.");
        return unitManagement;
    }

    private RepositoryException extractAndRepackCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        while (cause != null && !(cause instanceof StaleObjectStateException)) {
            cause = cause.getCause();
        }
        if (cause != null) {
            StaleObjectStateException staleObjectStateException = (StaleObjectStateException) cause;
            String entityName = staleObjectStateException.getEntityName();
            Serializable identifier = staleObjectStateException.getIdentifier();
            RepositoryException re = new RepositoryException("Stale object encountered", throwable);
            re.setEntityClassName(entityName);
            re.setEntityIdentifier(identifier.toString());
            String[] names = findEntityName(entityName, identifier);
            re.setEntityName(names[0]);
            re.setEntityAsString(names[1]);
            return re;
        }
        return new RepositoryException("Unknown DataStorage Exception", throwable);
    }

    private String[] findEntityName(String entityClassName, Serializable identifier) {
        String[] result = new String[2];
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            Class<?> entityClass = Class.forName(entityClassName);
            Object entity = entityManager.find(entityClass, identifier);
            if (entity != null) {
                Method getNameMethod = entity.getClass().getMethod("getName");
                Object name = getNameMethod.invoke(entity);
                result[0] = name.toString();
                result[1] = entity.toString();
            }
        } catch (Exception ignore) {
            logger.error("unable to find entity " + entityClassName + "#" + identifier.toString());
        } finally {
            if (entityManager != null)
                entityManager.close();
        }
        return result;
    }

    @Override
    public String getUrl() {
        return hostName;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    @Override
    public UserManagement loadUserManagement() throws RepositoryException {
        EntityManager entityManager = getEntityManager();
        UserManagement userManagement = null;
        try {
            userManagement = entityManager.find(UserManagement.class, UserManagementFactory.IDENTIFIER);
        } catch (Exception e) {
            throw new RepositoryException("Loading UserManagement failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
        if (userManagement == null)
            throw new RepositoryException("UserManagement not found.");
        return userManagement;
    }

    @Override
    public SystemModel storeSystemModel(SystemModel modelNode) throws RepositoryException {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            if (modelNode.getId() == 0) {
                entityManager.persist(modelNode);
            } else {
                modelNode = entityManager.merge(modelNode);
            }
            transaction.commit();
        } catch (Exception e) {
            throw new RepositoryException("Storing SystemModel failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
        return modelNode;
    }

    @Override
    public SystemModel loadSystemModel(long systemModelId) throws RepositoryException {
        EntityManager entityManager = getEntityManager();
        SystemModel systemModel = null;
        try {
            systemModel = entityManager.find(SystemModel.class, systemModelId);
        } catch (Exception e) {
            throw new RepositoryException("Loading SystemModel failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
        if (systemModel == null)
            throw new RepositoryException("SystemModel not found.");
        return systemModel;
    }

    public List<ParameterRevision> getChangeHistory(ParameterModel parameterModel) throws RepositoryException {
        final AuditReader reader = AuditReaderFactory.get(getEntityManager());
        final long pk = parameterModel.getId();

        List<Object[]> revisions = reader.createQuery()
                .forRevisionsOfEntity(ParameterModel.class, false, true)
                .add(AuditEntity.id().eq(pk))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        List<ParameterRevision> revisionList = new ArrayList<>(revisions.size());
        for (Object[] array : revisions) {
            ParameterModel versionedParameterModel = (ParameterModel) array[0];
            CustomRevisionEntity revisionEntity = (CustomRevisionEntity) array[1];
            RevisionType revisionType = (RevisionType) array[2];

            ParameterRevision parameterRevision = new ParameterRevision(versionedParameterModel, revisionEntity, revisionType);
            revisionList.add(parameterRevision);
        }

        return revisionList;
    }

    @Override
    public void storeLog(LogEntry logEntry) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(logEntry);
            transaction.commit();
        } catch (Exception e) {
            logger.debug("logging action to database failed: " + e.getMessage());
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public Long getLastStudyModification(String name) {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            final CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
            final Root<Study> studyRoot = criteriaQuery.from(Study.class);
            final Predicate namePredicate = criteriaBuilder.equal(studyRoot.get("name"), name);
            criteriaQuery.where(namePredicate);
            criteriaQuery.select(criteriaBuilder.tuple(studyRoot.get("latestModelModification")));
            Tuple result = entityManager.createQuery(criteriaQuery).getSingleResult();
            Long timestamp = (Long) result.get(0);
            return timestamp;
        } catch (Exception e) {
            logger.warn("loading last modification of study failed.", e);
            return null;
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public ExternalModel storeExternalModel(ExternalModel externalModel) throws RepositoryException {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            if (externalModel.getId() == 0) {
                entityManager.persist(externalModel);
            } else {
                externalModel = entityManager.merge(externalModel);
            }
            transaction.commit();
        } catch (Exception e) {
            throw new RepositoryException("Storing ExternalModel failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
        return externalModel;
    }

    @Override
    public ExternalModel loadExternalModel(long externalModelId) throws RepositoryException {
        EntityManager entityManager = getEntityManager();
        ExternalModel externalModel = null;
        try {
            externalModel = entityManager.find(ExternalModel.class, externalModelId);
            //entityManager.refresh(externalModel);
        } catch (Exception e) {
            throw new RepositoryException("Loading ExternalModel failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
        if (externalModel == null)
            throw new RepositoryException("ExternalModel not found.");
        return externalModel;
    }

    @Override
    public void close() {
        releaseEntityManagerFactory();
    }

    @Override
    public void finalize() throws Throwable {
        releaseEntityManagerFactory();
        super.finalize();
    }

    @Override
    public String toString() {
        return "DatabaseStorage{" +
                "hostName='" + hostName + '\'' +
                "schema='" + schema + '\'' +
                "persistenceUnit='" + persistenceUnit + '\'' +
                "properties=" + properties +
                '}';
    }

    private EntityManager getEntityManager() throws RepositoryException {
        if (emf == null) {
            try {
                emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
            } catch (Exception e) {
                logger.fatal("connecting to database failed!", e);
                throw new RepositoryException("database connection failed");
            }
        }
        return emf.createEntityManager();
    }

    private void releaseEntityManagerFactory() {
        if (emf != null) {
            try {
                emf.close();
            } catch (Exception ignore) {
            }
            emf = null;
        }
    }
}
