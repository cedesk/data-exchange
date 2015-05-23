package ru.skoltech.cedl.dataexchange.repository;

import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

public class DBUtil {

    private static EntityManager getEntityManager() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("javax.persistence.jdbc.url", "jdbc:mysql://localhost:3306/cedesk_dev");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("db", map);
        return emf.createEntityManager();
    }

    public static void storeStudy(Study study) {
        EntityManager entityManager = getEntityManager();
        entityManager.setFlushMode(FlushModeType.AUTO);
        //EntityTransaction transaction = entityManager.getTransaction();
        //transaction.begin();
        entityManager.persist(study);
        //transaction.commit();
    }

    public static Study loadStudy() {
        EntityManager entityManager = getEntityManager();
        try {
            Study reference = entityManager.getReference(Study.class, 1L);
            return reference;
        } catch (EntityNotFoundException e) {
            System.err.println("WARNING! Study not found.");
            return null;
        }
    }

    public static void storeModel(SystemModel modelNode) {
        EntityManager entityManager = getEntityManager();
        //entityManager.setFlushMode(FlushModeType.AUTO);
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        entityManager.persist(modelNode);
        transaction.commit();
        entityManager.close();
    }

    public static ModelNode loadModel() {
        EntityManager entityManager = getEntityManager();
        return entityManager.getReference(ModelNode.class, 1L);
    }

}