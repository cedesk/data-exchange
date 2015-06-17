package ru.skoltech.cedl.dataexchange.repository;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Map;

/**
 * Created by dknoll on 24/05/15.
 */
public class DatabaseStorage implements Repository {

    public static final String JAVAX_PERSISTENCE_JDBC_URL = "javax.persistence.jdbc.url";
    private static final Logger logger = Logger.getLogger(DatabaseStorage.class);
    private static final String LOCALHOST = "localhost";

    private String hostName;

    private EntityManagerFactory emf;

    /**
     * The default backend uses a DB on the localhost.
     */
    DatabaseStorage() {
        this(LOCALHOST);
    }

    DatabaseStorage(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public void storeStudy(Study study) throws RepositoryException {
        try {
            EntityManager entityManager = getEntityManager();
            entityManager.setFlushMode(FlushModeType.AUTO);
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(study);
            transaction.commit();
        } catch (Exception e) {
            throw new RepositoryException("Storing Study failed.", e);
        } finally {
            try {
                getEntityManager().close();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public Study loadStudy(String name) throws RepositoryException {
        EntityManager entityManager = getEntityManager();
        try {
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
        } finally {
            try {
                getEntityManager().close();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void storeUserRoleManagement(UserRoleManagement userRoleManagement) throws RepositoryException {
        try {
            EntityManager entityManager = getEntityManager();
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(userRoleManagement);
            transaction.commit();
        } catch (Exception e) {
            throw new RepositoryException("Storing UserRoleManagement failed.", e);
        } finally {
            try {
                getEntityManager().close();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void storeUserManagement(UserManagement userManagement) throws RepositoryException {
        try {
            EntityManager entityManager = getEntityManager();
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(userManagement);
            transaction.commit();
        } catch (Exception e) {
            throw new RepositoryException("Storing UserManagement failed.", e);
        } finally {
            try {
                getEntityManager().close();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public UserRoleManagement loadUserRoleManagement(long studyId) throws RepositoryException {
        EntityManager entityManager = getEntityManager();
        UserRoleManagement userRoleManagement = null;
        try {
            userRoleManagement = entityManager.find(UserRoleManagement.class, studyId);
            if (userRoleManagement == null)
                throw new RepositoryException("UserRoleManagement not found.");
        } catch (Exception e) {
            throw new RepositoryException("Loading UserRoleManagement failed.", e);
        } finally {
            try {
                getEntityManager().close();
            } catch (Exception ignore) {
            }
        }
        return userRoleManagement;
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
            if (userManagement == null)
                throw new RepositoryException("UserManagement not found.");
        } catch (Exception e) {
            throw new RepositoryException("Loading UserManagement failed.", e);
        } finally {
            try {
                getEntityManager().close();
            } catch (Exception ignore) {
            }
        }
        return userManagement;
    }

    @Override
    public void storeSystemModel(SystemModel modelNode) throws RepositoryException {
        try {
            EntityManager entityManager = getEntityManager();
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            entityManager.persist(modelNode);
            transaction.commit();
        } catch (Exception e) {
            throw new RepositoryException("Storing SystemModel failed.", e);
        } finally {
            try {
                getEntityManager().close();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public SystemModel loadSystemModel(long studyId) throws RepositoryException {
        EntityManager entityManager = getEntityManager();
        try {
            SystemModel systemModel = entityManager.find(SystemModel.class, studyId);
            if (systemModel == null)
                throw new RepositoryException("SystemModel not found.");
            return systemModel;
        } catch (Exception e) {
            throw new RepositoryException("Loading SystemModel failed.", e);
        }
    }

    private EntityManager getEntityManager() {
        if (emf == null) {
            if (hostName.equals(LOCALHOST)) {
                emf = Persistence.createEntityManagerFactory("db");
            } else {
                Map<String, Object> properties = emf.getProperties();
                String jdbcUrl = (String) properties.get(JAVAX_PERSISTENCE_JDBC_URL);
                String newUrl = jdbcUrl.replace(LOCALHOST, hostName);
                properties.put(JAVAX_PERSISTENCE_JDBC_URL, newUrl);
                emf = Persistence.createEntityManagerFactory("db", properties);
            }
        }
        return emf.createEntityManager();
    }

    private void releaseEntityManagerFactory() {
        emf.close();
        emf = null;
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
}
