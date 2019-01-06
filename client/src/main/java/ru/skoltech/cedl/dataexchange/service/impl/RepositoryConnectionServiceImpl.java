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
import ru.skoltech.cedl.dataexchange.repository.ConnectionVerifier;
import ru.skoltech.cedl.dataexchange.service.RepositoryConnectionService;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public boolean checkRepositoryConnection(String hostName, String schema, String userName, String password) {

        boolean serverReachable = ConnectionVerifier.isServerReachable(hostName, 500);
        if (serverReachable) {
            logger.info("check server reachable (" + hostName + ") ... succeeded!");
        } else {
            logger.warn("check server reachable (" + hostName + ") ... failed!");
            return false;
        }
        String url = this.createRepositoryUrl(hostName, schema);
        int port = getPort(url);
        boolean serverListening = ConnectionVerifier.isServerListening(hostName, port, 500);
        if (serverListening) {
            logger.info("check server port listening (" + port + ") ... succeeded!");
        } else {
            logger.warn("check server port listening (" + port + ") ... failed!");
            return false;
        }

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

    private int getPort(String jdbcUrl) {
        try {
            String regex = ".*://(\\w*):(\\d++)/.*";
            Pattern p = Pattern.compile(regex);
            Matcher matcher = p.matcher(jdbcUrl);
            if (matcher.find()) {
                return Integer.valueOf(matcher.group(2));
            }
        } catch (Exception e) {
            logger.warn("server URL is malformed", e);
        }
        return -1;
    }

    @Override
    public String createRepositoryUrl(String repositoryHost, String repositorySchemaName) {
        String defaultJdbcUrlPattern = applicationSettings.getRepositoryJdbcUrlPattern();
        return String.format(defaultJdbcUrlPattern, repositoryHost, repositorySchemaName);
    }

    @Override
    public String createRepositoryUrl() {
        String repositoryHost = applicationSettings.getRepositoryHost();
        String repositorySchemaName = applicationSettings.getRepositorySchemaName();
        return this.createRepositoryUrl(repositoryHost, repositorySchemaName);
    }

}
