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

import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.unit.Prefix;
import ru.skoltech.cedl.dataexchange.entity.unit.QuantityKind;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Operations with file system.
 * <p>
 * Created by Nikolay Groshkov on 06-Jul-17.
 */
public interface FileStorageService {

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
     * Import {@link Calculation} from the file.
     *
     * @param inputFile file which stores {@link Calculation} to import
     * @return imported calculation
     * @throws IOException if import is impossible
     */
    Calculation importCalculation(File inputFile) throws IOException;

    /**
     * Import {@link Study} from the zip file.
     *
     * @param inputFile file which stores {@link Study} to import
     * @return imported study
     * @throws IOException if import is impossible
     */
    Study importStudyFromZip(File inputFile) throws IOException;

    /**
     * Import {@link Study} from the file.
     *
     * @param inputFile file which stores {@link Study} to import
     * @return imported study
     * @throws IOException if import is impossible
     */
    Study importStudy(File inputFile) throws IOException;

    /**
     * Import {@link SystemModel} from the file.
     *
     * @param inputFile file which stores {@link SystemModel} to import
     * @return imported system model
     * @throws IOException if import is impossible
     */
    SystemModel importSystemModel(File inputFile) throws IOException;

    /**
     * Import list of {@link Prefix}es from the file.
     *
     * @param resource resource which contains list of {@link Prefix}es to import
     * @return imported list of {@link Prefix}es
     * @throws IOException if import is impossible
     */
    List<Prefix> importPrefixes(String resource) throws IOException;

    /**
     * Import list of {@link Unit}s from the file.
     *
     * @param resource resource which contains list of {@link Unit}s to import
     * @return imported list of {@link Unit}s
     * @throws IOException if import is impossible
     */
    List<Unit> importUnits(String resource) throws IOException;

    /**
     * Import list of {@link QuantityKind}s from the file.
     *
     * @param resource resource which contains list of {@link QuantityKind}s to import
     * @return imported list of {@link QuantityKind}s
     * @throws IOException if import is impossible
     */
    List<QuantityKind> importQuantityKinds(String resource) throws IOException;

    /**
     * Import {@link UserRoleManagement} from the file.
     *
     * @param inputFile file which stores {@link UserRoleManagement} to import
     * @return imported user role management
     * @throws IOException if import is impossible
     */
    UserRoleManagement importUserRoleManagement(File inputFile) throws IOException;

    /**
     * Export {@link Calculation} to the file.
     *
     * @param calculation {@link Calculation} to export
     * @param outputFile output file to export
     * @throws IOException if export is impossible
     */
    void exportCalculation(Calculation calculation, File outputFile) throws IOException;

    /**
     * Export {@link Study} to the file.
     *
     * @param study      {@link Study} to export
     * @param outputFile output xml file to export
     * @throws IOException if export is impossible
     */
    void exportStudy(Study study, File outputFile) throws IOException;

    /**
     * Export {@link Study} to the zip file.
     *
     * @param study      {@link Study} to export
     * @param outputFile output zip file to export
     * @throws IOException if export is impossible
     */
    void exportStudyToZip(Study study, File outputFile) throws IOException;

    /**
     * Export {@link SystemModel} in the file.
     *
     * @param systemModel {@link SystemModel} to export
     * @param outputFile  output file to export
     * @throws IOException if export is impossible
     */
    void exportSystemModel(SystemModel systemModel, File outputFile) throws IOException;

    /**
     * Export current system {@link Unit}s (along with the {@link Prefix}es and {@link QuantityKind}s) in the file.
     *
     * @param outputFile output file to export
     * @throws IOException if export is impossible
     */
    void exportUnits(File outputFile) throws IOException;

    /**
     * Export {@link UserRoleManagement} in the file.
     *
     * @param userRoleManagement {@link UserRoleManagement} to export
     * @param outputFile output file to export
     * @throws IOException if export is impossible
     */
    void exportUserRoleManagement(UserRoleManagement userRoleManagement, File outputFile) throws IOException;
}
