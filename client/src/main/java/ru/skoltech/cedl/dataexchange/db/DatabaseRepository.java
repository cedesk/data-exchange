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
import java.util.*;

/**
 * Created by dknoll on 24/05/15.
 */
public class DatabaseRepository implements Repository {

    private static final Logger logger = Logger.getLogger(DatabaseRepository.class);

    private static final String JDBC_URL_PATTERN = "jdbc:mysql://%s:3306/%s?serverTimezone=UTC";

    private final String persistenceUnit;
    private final String hostName;
    private final String schema;
    private final Map<String, Object> properties;

    private EntityManagerFactory emf;

    /**
     * The default backend uses a DB on the localhost.
     * For test purposes.
     */
    public DatabaseRepository(String persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
        this.hostName = null;
        this.schema = null;
        this.properties = Collections.emptyMap();
    }

    public DatabaseRepository(String persistenceUnit, String hostName, String schema, String userName, String password) {
        this.persistenceUnit = persistenceUnit;
        this.hostName = hostName;
        this.schema = schema;

        final String url = String.format(JDBC_URL_PATTERN, hostName, schema);
        logger.debug("repository url: " + url + ", user: " + userName);

        Map<String, Object> properties = new HashMap<>(3);
        properties.put(PERSISTENCE_URL_PROPERTY, url);
        properties.put(PERSISTENCE_USER_PROPERTY, userName);
        properties.put(PERSISTENCE_PASSWORD_PROPERTY, password);
        this.properties = Collections.unmodifiableMap(properties);
    }

    public EntityManager getEntityManager() throws RepositoryException {
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

    public static boolean checkDatabaseConnection(String hostName, String schema, String userName, String password) {
        final String url = String.format(JDBC_URL_PATTERN, hostName, schema);
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
    public void close() {
        releaseEntityManagerFactory();
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
    public void finalize() throws Throwable {
        releaseEntityManagerFactory();
        super.finalize();
    }

    public List<ParameterRevision> getChangeHistory(ParameterModel parameterModel) throws RepositoryException {
        EntityManager entityManager = null;
        try {
            entityManager = getEntityManager();
            final AuditReader reader = AuditReaderFactory.get(entityManager);
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
                try {
                    // dummy operation to ensure properties to be loaded (overcome lazy loading, which would fail due to closed db connection)
                    int ignore = parameterRevision.toString().length();
                } catch (Exception e) {
                    logger.error("problem initializing parameter revision properties, " + e.getMessage());
                }
                revisionList.add(parameterRevision);
            }

            return revisionList;
        } catch (Exception e) {
            throw new RepositoryException("Loading revision history failed.", e);
        } finally {
            try {
                if (entityManager != null)
                    entityManager.close();
            } catch (Exception ignore) {
            }
        }
    }

    public CustomRevisionEntity getLastRevision(PersistedEntity persistedEntity) {
        EntityManager entityManager = null;
        try {
            final long pk = persistedEntity.getId();
            if (pk == 0) return null; // quick exit for unpersisted entities
            entityManager = getEntityManager();
            final AuditReader reader = AuditReaderFactory.get(entityManager);

            Object[] array = (Object[]) reader.createQuery()
                    .forRevisionsOfEntity(persistedEntity.getClass(), false, true)
                    .add(AuditEntity.id().eq(pk))
                    .addOrder(AuditEntity.revisionNumber().desc()).setMaxResults(1)
                    .getSingleResult();

            return (CustomRevisionEntity) array[1];
        } catch (Exception e) {
            logger.debug("Loading revision history failed: " +
                    persistedEntity.getClass().getSimpleName() + "[" + persistedEntity.getId() + "]");
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
        } catch (NoResultException e) {
            logger.warn("study not stored!");
            return null;
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
    public UserRoleManagement loadUserRoleManagement(long id) throws RepositoryException {
        EntityManager entityManager = getEntityManager();
        UserRoleManagement userRoleManagement = null;
        try {
            userRoleManagement = entityManager.find(UserRoleManagement.class, id);
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
    public String toString() {
        return "DatabaseRepository{" +
                "hostName='" + hostName + '\'' +
                "schema='" + schema + '\'' +
                "persistenceUnit='" + persistenceUnit + '\'' +
                "properties=" + properties +
                '}';
    }

    @Override
    public boolean updateDatabaseScheme() {
        EntityManagerFactory entityManagerFactory = null;
        EntityManager entityManager = null;
        logger.info("updating database scheme");
        try {
            Properties properties = new Properties();
            properties.putAll(this.properties);
            properties.put(HIBERNATE_TABLE_MAPPING, HIBERNATE_TABLE_MAPPING_UPDATE);
            entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit, properties);
            entityManager = entityManagerFactory.createEntityManager();
        } catch (Exception e) {
            StatusLogger.getInstance().log("Database scheme update failed!", true);
            logger.error("Database scheme update failed!", e);
            return false;
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

    @Override
    public boolean validateDatabaseScheme() {
        EntityManagerFactory entityManagerFactory = null;
        EntityManager entityManager = null;
        logger.info("validating database scheme");
        try {
            Properties properties = new Properties();
            properties.putAll(this.properties);
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

    private ApplicationProperty loadApplicationProperty(ApplicationProperty applicationProperty) throws RepositoryException {
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
        return appProp;
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
}
