package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;

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

    private static final String LOCALHOST = "localhost";

    private EntityManager em;

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
    public void storeStudy(Study study) {
        EntityManager entityManager = getEntityManager();
        entityManager.setFlushMode(FlushModeType.AUTO);
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(study);
        transaction.commit();
    }

    @Override
    public Study loadStudy(String name) {
        EntityManager entityManager = getEntityManager();
        try {
            final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            final CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(Study.class);
            final Root stateRoot = criteriaQuery.from(Study.class);
            Predicate predicate2 = criteriaBuilder.equal(stateRoot.get("name"), name);
            //criteriaQuery.select(criteriaBuilder.construct(Study.class));
            criteriaQuery.where(predicate2);
            final TypedQuery query = entityManager.createQuery(criteriaQuery);
            Object singleResult = query.getSingleResult();
            return (Study) singleResult;
        } catch (EntityNotFoundException e) {
            System.err.println("WARNING! Study not found.");
            return null;
        }
    }

    @Override
    public void storeUserManagement(UserManagement userManagement) {
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(userManagement);
        transaction.commit();
    }

    @Override
    public UserManagement loadUserManagement(long studyId) {
        EntityManager entityManager = getEntityManager();
        UserManagement userManagement = null;
        try {
            userManagement = entityManager.getReference(UserManagement.class, studyId);
        } catch (EntityNotFoundException e) {
            System.err.println("WARNING! UserManagement not found.");
        }
        return userManagement;
    }

    @Override
    public void storeSystemModel(SystemModel modelNode) {
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(modelNode);
        transaction.commit();
    }

    @Override
    public SystemModel loadSystemModel(long studyId) {
        EntityManager entityManager = getEntityManager();
        SystemModel systemModel = null;
        try {
            systemModel = entityManager.getReference(SystemModel.class, studyId);
        } catch (EntityNotFoundException e) {
            System.err.println("WARNING! Model not found.");
        }
        return systemModel;
    }

    private EntityManager getEntityManager() {
        if (em == null) {
            emf = Persistence.createEntityManagerFactory("db");
            if (!hostName.equals(LOCALHOST)) {
                Map<String, Object> properties = emf.getProperties();
                String jdbcUrl = (String) properties.get(JAVAX_PERSISTENCE_JDBC_URL);
                String newUrl = jdbcUrl.replace(LOCALHOST, hostName);
                properties.put(JAVAX_PERSISTENCE_JDBC_URL, newUrl);
                emf = Persistence.createEntityManagerFactory("db", properties);
            }
            em = emf.createEntityManager();
        }
        return em;
    }

    private void releaseEntityManager() {
        em.close();
        emf.close();
    }

    @Override
    public void close() {
        releaseEntityManager();
    }

    @Override
    public void finalize() throws Throwable {
        releaseEntityManager();
        super.finalize();
    }

    @Override
    public String toString() {
        return "DatabaseStorage{" +
                "hostName='" + hostName + '\'' +
                '}';
    }

}
