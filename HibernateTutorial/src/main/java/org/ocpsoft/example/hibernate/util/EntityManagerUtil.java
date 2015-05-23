package org.ocpsoft.example.hibernate.util;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class EntityManagerUtil {
    public static EntityManagerFactory getEntityManagerFactory() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("javax.persistence.jdbc.url", "jdbc:mysql://localhost:3306/cedesk_dev");
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("db", map);
        return emf;
    }
}