package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dknoll on 24/05/15.
 */
public class DatabaseStorage {

    private static final String LOCALHOST = "localhost";

    private EntityManager em;

    private String hostName;

    /**
     * The default backend uses a DB on the localhost.
     */
    public DatabaseStorage() {
        this(LOCALHOST);
    }

    public DatabaseStorage(String hostName) {
        this.hostName = hostName;
    }

    public void storeStudy(Study study) {
        EntityManager entityManager = getEntityManager();
        entityManager.setFlushMode(FlushModeType.AUTO);
        //EntityTransaction transaction = entityManager.getTransaction();
        //transaction.begin();
        entityManager.persist(study);
        //transaction.commit();
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

    public void storeModel(SystemModel modelNode) {
        EntityManager entityManager = getEntityManager();
        //entityManager.setFlushMode(FlushModeType.AUTO);
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(modelNode);
        transaction.commit();
    }

    public SystemModel loadModel() {
        EntityManager entityManager = getEntityManager();
        return entityManager.getReference(SystemModel.class, 1L);
    }

    private EntityManager getEntityManager() {
        if (em == null) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("javax.persistence.jdbc.url", "jdbc:mysql://" + hostName + ":3306/cedesk_dev");
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("db", map);
            em = emf.createEntityManager();
        }
        return em;
    }

    public void finalize() throws Throwable {
        em.close();
        super.finalize();
    }
}
