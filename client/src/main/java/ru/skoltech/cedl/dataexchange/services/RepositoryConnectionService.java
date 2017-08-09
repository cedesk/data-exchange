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

package ru.skoltech.cedl.dataexchange.services;

/**
 * Service for repository connection operations.
 *
 * Created by Nikolay Groshkov on 04-Aug-17.
 */
public interface RepositoryConnectionService {

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
     * Check database connection based on passed parameters.
     *
     * @param hostName name of host server
     * @param schema name of schema
     * @param userName name of user
     * @param password user password
     * @return <i>true</i> if connection is possible, <i>false</i> - if opposite
     */
    boolean checkRepositoryConnection(String hostName, String schema, String userName, String password);

}
