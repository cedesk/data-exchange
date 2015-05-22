package org.ocpsoft.example.hibernate.util;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerUtil {
    public static EntityManagerFactory getEntityManagerFactory() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("db");
        return emf;
    }
}