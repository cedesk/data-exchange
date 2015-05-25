package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dknoll on 24/05/15.
 */
public class DatabaseStorage implements Repository {

    private static final String URL = "jdbc:mysql://HOSTNAME:3306/cedesk_dev";
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

    public void storeStudy(Study study) {
        EntityManager entityManager = getEntityManager();
        entityManager.setFlushMode(FlushModeType.AUTO);
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(study);
        transaction.commit();
    }

    public Study loadStudy() {
        EntityManager entityManager = getEntityManager();
        try {
            Study reference = entityManager.getReference(Study.class, 1L);
            return reference;
        } catch (EntityNotFoundException e) {
            System.err.println("WARNING! Study not found.");
            return null;
        }
    }

    public void storeSystemModel(SystemModel modelNode) {
        EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(modelNode);
        transaction.commit();
    }

    public SystemModel loadSystemModel() {
        EntityManager entityManager = getEntityManager();
        SystemModel systemModel = null;
        try {
            systemModel = entityManager.getReference(SystemModel.class, 1L);
        } catch (EntityNotFoundException e) {
            System.err.println("WARNING! Model not found.");
        }
        return systemModel;
    }

    private EntityManager getEntityManager() {
        if (em == null) {
            Map<String, String> map = new HashMap<String, String>();
            String url = URL.replace("HOSTNAME", hostName);
            map.put("javax.persistence.jdbc.url", url);
            emf = Persistence.createEntityManagerFactory("db", map);
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
