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

import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.unit.UnitManagement;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Operations with file system.
 * <p>
 * Created by Nikolay Groshkov on 06-Jul-17.
 */
public interface FileStorageService {

    /**
     * Retrieve a basic application directory.
     *
     * @return application directory.
     */
    File applicationDirectory();

    /**
     * Check file existence and non emptiness.
     *
     * @param file file to check
     * @return true if file exists and is not empty, false in case of opposite
     */
    boolean checkFileExistenceAndNonEmptiness(File file);

    /**
     * Create directory by path.
     *
     * @param path path to directory
     */
    void createDirectory(File path);

    /**
     * Retrieve a data directory.
     *
     * @param repositoryUrl    repository url
     * @param repositoryScheme repository scheme
     * @param projectName      project name
     * @return data directory
     */
    File dataDir(String repositoryUrl, String repositoryScheme, String projectName);

    /**
     * Load {@link Calculation} from the file.
     *
     * @param inputFile file which stores {@link Calculation}
     * @return calculation
     * @throws IOException if loading is impossible
     */
    Calculation loadCalculation(File inputFile) throws IOException;

    /**
     * Load {@link SystemModel} from the file.
     *
     * @param inputFile file which stores {@link SystemModel}
     * @return system model
     * @throws IOException if loading is impossible
     */
    SystemModel loadSystemModel(File inputFile) throws IOException;

    /**
     * Load {@link UnitManagement} from the file.
     *
     * @param inputStream inputStream which contains {@link UnitManagement}
     * @return unit management
     * @throws IOException if loading is impossible
     */
    UnitManagement loadUnitManagement(InputStream inputStream) throws IOException;

    /**
     * Load {@link UserRoleManagement} from the file.
     *
     * @param inputFile file which stores {@link UserRoleManagement}
     * @return user role management
     * @throws IOException if loading is impossible
     */
    UserRoleManagement loadUserRoleManagement(File inputFile) throws IOException;

    /**
     * Store {@link Calculation} in the file.
     *
     * @param calculation {@link Calculation} for storage
     * @param outputFile  output file for storage
     * @throws IOException if storage is impossible
     */
    void storeCalculation(Calculation calculation, File outputFile) throws IOException;

    /**
     * Store {@link SystemModel} in the file.
     *
     * @param systemModel {@link SystemModel} for storage
     * @param outputFile  output file for storage
     * @throws IOException if storage is impossible
     */
    void storeSystemModel(SystemModel systemModel, File outputFile) throws IOException;

    /**
     * Store {@link UnitManagement} in the file.
     *
     * @param unitManagement {@link UnitManagement} for storage
     * @param outputFile     output file for storage
     * @throws IOException if storage is impossible
     */
    void storeUnitManagement(UnitManagement unitManagement, File outputFile) throws IOException;

    /**
     * Store {@link UserRoleManagement} in the file.
     *
     * @param userRoleManagement {@link UserRoleManagement} for storage
     * @param outputFile         output file for storage
     * @throws IOException if storage is impossible
     */
    void storeUserRoleManagement(UserRoleManagement userRoleManagement, File outputFile) throws IOException;
}
