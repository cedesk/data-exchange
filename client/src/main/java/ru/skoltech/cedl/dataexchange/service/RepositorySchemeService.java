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

package ru.skoltech.cedl.dataexchange.service;

import ru.skoltech.cedl.dataexchange.db.RepositoryException;

import javax.transaction.Transactional;

/**
 * Operations with repository scheme versions.
 * <p>
 * Created by Nikolay Groshkov on 07-Aug-17.
 */
public interface RepositorySchemeService {

    /**
     * Validate and store scheme version.
     *
     * @return <i>true</i> - if validate operation was performed successfully, <i>false</i> - if opposite
     * @throws RepositoryException if actual and repository versions are incompatible
     */
    @Transactional
    boolean checkAndStoreSchemeVersion() throws RepositoryException;

    /**
     * Validate scheme version.
     *
     * @return <i>true</i> - if validate operation was performed successfully, <i>false</i> - if opposite
     * @throws RepositoryException if actual and repository versions are incompatible
     */
    boolean checkSchemeVersion() throws RepositoryException;

}
