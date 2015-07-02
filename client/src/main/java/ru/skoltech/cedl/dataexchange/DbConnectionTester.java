package ru.skoltech.cedl.dataexchange;

import ru.skoltech.cedl.dataexchange.repository.Repository;
import ru.skoltech.cedl.dataexchange.repository.RepositoryFactory;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by D.Knoll on 24.06.2015.
 */
public class DbConnectionTester {
    public static final String PERSISTENCE_URL_PROPERTY = "javax.persistence.jdbc.url";
    public static final String PERSISTENCE_USER_PROPERTY = "javax.persistence.jdbc.user";
    public static final String PERSISTENCE_PASSWORD_PROPERTY = "javax.persistence.jdbc.password";

    public static final String DEFAULT_JDBC_URL = "jdbc:mysql://localhost:3306/cedesk_dev";
    public static final String DEFAULT_REPOSITORY_HOST_NAME = "localhost";
    private static final String LOCALHOST = "localhost"; // matches hostname in JDBC url in persistence.xml

    public static void main(String... arguments) throws Exception {

        System.out.println("-------- MySQL JDBC Connection Testing ------------");

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
            return;
        }

        System.out.println("MySQL JDBC Driver Registered!");
        Connection connection = null;

        try {
            connection = DriverManager
                    .getConnection("jdbc:mysql://srv-cedl-01.skoltech.ru:3306/cedesk_dev", "cedesk", "cedesk");
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;
        }

        if (connection != null) {
            System.out.println("Connection successful: " + connection.getMetaData().toString());
        } else {
            System.out.println("Failed to make connection!");
        }

        System.out.println("\n\n\n---------------------------------- AAA ----------------------------------------------\n\n\n");

        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", "jdbc:mysql://srv-cedl-01.skoltech.ru:3306/cedesk_dev");
        properties.put("javax.persistence.jdbc.user", "cedesk");
        properties.put("javax.persistence.jdbc.password", "cedesk");
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("db", properties);
        entityManagerFactory.close();

        System.out.println("\n\n\n----------------------------------- BBB -----------------------------------------------\n\n\n");

        Map<String, String> props = new HashMap<>();
        final String hostName = "srv-cedl-01.skoltech.ru";
        String url = DEFAULT_JDBC_URL.replace(LOCALHOST, hostName);
        props.put(PERSISTENCE_URL_PROPERTY, url);
        props.put(PERSISTENCE_USER_PROPERTY, "cedesk");
        props.put(PERSISTENCE_PASSWORD_PROPERTY, "cedesk");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("db", props);
        EntityManager entityManager = emf.createEntityManager();
        SystemModel model1 = entityManager.find(SystemModel.class, 1L);
        emf.close();

        System.out.println("\n\n\n--------------------------------- CCC -----------------------------------------------\n\n\n");

        Repository repository = RepositoryFactory.getDatabaseRepository();
        SystemModel model2 = repository.loadSystemModel(1);
        repository.close();
    }
}
