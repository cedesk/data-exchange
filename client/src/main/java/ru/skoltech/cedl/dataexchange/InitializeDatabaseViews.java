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

package ru.skoltech.cedl.dataexchange;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Execute SQL script upon application start.
 * <p>
 * Created by Nikolay Groshkov on 31-Jan-18.
 */
public class InitializeDatabaseViews {

    private static final Logger logger = Logger.getLogger(InitializeDatabaseViews.class);

    private JdbcTemplate jdbcTemplate;

    private ApplicationSettings applicationSettings;

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void init() throws SQLException {
        if (applicationSettings.isRepositorySchemaCreate()) {
            try {
                logger.info("Recreating views in DB.");
                Connection connection = jdbcTemplate.getDataSource().getConnection();
                Resource createViewsScript = new ClassPathResource("create-views.sql");
                ScriptUtils.executeSqlScript(connection, createViewsScript);
            } catch (SQLException e) {
                logger.error("Failed to create views in DB.", e);
                throw e;
            }
        }
    }
}
