package ru.skoltech.cedl.dataexchange.db;

import org.apache.log4j.Logger;
import org.hibernate.StaleObjectStateException;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.structure.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterRevision;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
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

    private final static Logger logger = Logger.getLogger(DatabaseStorage.class);

    public static final String PERSISTENCE_URL_PROPERTY = "javax.persistence.jdbc.url";
    public static final String PERSISTENCE_USER_PROPERTY = "javax.persistence.jdbc.user";
    public static final String PERSISTENCE_PASSWORD_PROPERTY = "javax.persistence.jdbc.password";
    public static final String DEFAULT_USER_NAME = "cedesk";
    public static final String DEFAULT_PASSWORD = "cedesk";
    public static final String DEFAULT_HOST_NAME = "localhost";
    public static final String DB_PERSISTENCE_UNIT_NAME = "db";
    public final static String MEM_PERSISTENCE_UNIT_NAME = "mem";
    private static final String DEFAULT_JDBC_URL = "jdbc:mysql://HOSTNAME:3306/cedesk_dev";
    private static final String HOST_NAME = "HOSTNAME";
    private String hostName;
    private EntityManagerFactory emf;
    private String persistenceUnit;
    private Map<String, Object> properties = new HashMap<>();

    /**
     * The default backend uses a DB on the localhost.
     */
    public DatabaseStorage(String persistenceUnit) {
        this.persistenceUnit = persistenceUnit;
    }

    public DatabaseStorage(String persistenceUnit, String hostName, String userName, String password) {
        this.persistenceUnit = persistenceUnit;
        this.hostName = hostName;
        String url = DEFAULT_JDBC_URL.replace(HOST_NAME, hostName);
        properties.put(PERSISTENCE_URL_PROPERTY, url);
        properties.put(PERSISTENCE_USER_PROPERTY, userName);
        properties.put(PERSISTENCE_PASSWORD_PROPERTY, password);
    }

    public static boolean checkDatabaseConnection(String hostName, String user, String password) {
        String url = DEFAULT_JDBC_URL.replace(HOST_NAME, hostName);
        try {
            DriverManager.getConnection(url, user, password).close();
            // TODO: check also existence of schema "cedesk"
            logger.info("check of database connection succeeded!");
            return true;
        } catch (SQLException e) {
            logger.warn("check of database connection failed!");
            return false;
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
            entityManager.refresh(singleResult);
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

    private RepositoryException extractAndRepackCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        while (cause != null && !(cause instanceof StaleObjectStateException)) {
            cause = cause.getCause();
        }
        if (cause != null) {
            StaleObjectStateException staleObjectStateException = (StaleObjectStateException) cause;
            String entityName = staleObjectStateException.getEntityName();
            Serializable identifier = staleObjectStateException.getIdentifier();
            RepositoryException re = new RepositoryException("Stale object encountered");
            re.setEntityClassName(entityName);
            re.setEntityIdentifier(identifier.toString());
            EntityManager entityManager = null;
            try {
                entityManager = getEntityManager();
                Class<?> entityClass = Class.forName(entityName);
                Object entity = entityManager.find(entityClass, identifier);
                if (entity != null) {
                    re.setEntityAsString(entity.toString());
                    Method getNameMethod = entity.getClass().getMethod("getName");
                    Object name = getNameMethod.invoke(entity);
                    re.setEntityName(name.toString());
                }
            } catch (Exception ignore) {
            } finally {
                if (entityManager != null)
                    entityManager.close();
            }
            return re;
        }
        return new RepositoryException("Unknown DataStorage Exception", throwable);
    }

    @Override
    public String getUrl() {
        return hostName;
    }

    @Override
    public UserManagement loadUserManagement() throws RepositoryException {
        EntityManager entityManager = getEntityManager();
        UserManagement userManagement = null;
        try {
            userManagement = entityManager.find(UserManagement.class, 1L);
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
            entityManager.refresh(systemModel);
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

    private EntityManager getEntityManager() throws RepositoryException {
        if (emf == null) {
            try {
                emf = Persistence.createEntityManagerFactory(persistenceUnit, properties);
            } catch(Exception e) {
                logger.fatal("connecting to database failed!");
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
                '}';
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
            entityManager.refresh(externalModel);
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
}
