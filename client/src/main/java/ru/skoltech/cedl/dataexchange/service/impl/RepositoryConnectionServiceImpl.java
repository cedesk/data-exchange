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

package ru.skoltech.cedl.dataexchange.service.impl;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.service.RepositoryConnectionService;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Nikolay Groshkov on 04-Aug-17.
 */
public class RepositoryConnectionServiceImpl implements RepositoryConnectionService {

    private static final Logger logger = Logger.getLogger(RepositoryConnectionServiceImpl.class);

    private ApplicationSettings applicationSettings;

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    @Override
    public String createRepositoryUrl() {
        String repositoryHost = applicationSettings.getRepositoryHost();
        String repositorySchemaName = applicationSettings.getRepositorySchemaName();
        return this.createRepositoryUrl(repositoryHost, repositorySchemaName);
    }

    @Override
    public String createRepositoryUrl(String repositoryHost, String repositorySchemaName) {
        String defaultJdbcUrlPattern = applicationSettings.getRepositoryJdbcUrlPattern();
        return String.format(defaultJdbcUrlPattern, repositoryHost, repositorySchemaName);
    }

    @Override
    public boolean checkRepositoryConnection(String hostName, String schema, String userName, String password) {
        String url = this.createRepositoryUrl(hostName, schema);

        logger.debug("repository url: " + url + ", user: " + userName);
        try {
            DriverManager.getConnection(url, userName, password).close();
            logger.info("check of database connection succeeded!");
            return true;
        } catch (SQLException e) {
            logger.warn("check of database connection failed!");
            return false;
        }

    }

}
