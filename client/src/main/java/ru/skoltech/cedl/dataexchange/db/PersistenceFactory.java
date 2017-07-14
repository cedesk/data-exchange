package ru.skoltech.cedl.dataexchange.db;

import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Constructs a {@link DataSource} and {@link EntityManagerFactoryBuilder} objects.
 *
 * Created by n.groshkov on 30-Jun-17.
 */
public interface PersistenceFactory {

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
