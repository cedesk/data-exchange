package ru.skoltech.cedl.dataexchange.db;

import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Constructs a {@link DataSource} and {@link EntityManagerFactoryBuilder} objects.
 *
 * Created by Nikolay Groshkov on 30-Jun-17.
 */
public interface PersistenceFactory {

    /**
     * Create repository URL.
     *
     * @param hostName host name
     * @param schema schema name
     * @return repository URL
     */
    String createRepositoryUrl(String hostName, String schema);

    /**
     * Create database {@link DataSource}.
     *
     *  @return database {@link DataSource}
     */
    DataSource createDataSource();

    /**
     * Create {@link EntityManagerFactoryBuilder} based on default JPA properties.
     *
     * @return spring {@link EntityManagerFactoryBuilder}
     */
    EntityManagerFactoryBuilder createEntityManagerFactoryBuilder();

    /**
     * Create {@link EntityManagerFactoryBuilder} based on passed properties.
     *
     * @param jpaProperties JPA properties
     * @return spring {@link EntityManagerFactoryBuilder}
     */
    EntityManagerFactoryBuilder createEntityManagerFactoryBuilder(Map<String, ?> jpaProperties);

}
