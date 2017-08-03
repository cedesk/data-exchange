/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
     * Create repository URL with default hostname and schema.
     *
     * @return repository URL
     */
    String createRepositoryUrl();

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
