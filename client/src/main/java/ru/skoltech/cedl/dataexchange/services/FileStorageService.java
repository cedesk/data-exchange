package ru.skoltech.cedl.dataexchange.services;

import ru.skoltech.cedl.dataexchange.structure.model.Calculation;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Operations with file system.
 *
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
     * Retrieve a data directory.
     *
     * @param repositoryUrl repository url
     * @param repositoryScheme repository scheme
     * @param projectName project name
     * @return data directory
     */
    File dataDir(String repositoryUrl, String repositoryScheme, String projectName);

    /**
     * Create directory by path.
     *
     * @param path path to directory
     */
    void createDirectory(File path);

    /**
     * Check file existence and non emptiness.
     *
     * @param file file to check
     * @return true if file exists and is not empty, false in case of opposite
     */
    boolean checkFileExistenceAndNonEmptiness(File file);

    /**
     * Store {@link SystemModel} in the file.
     *
     * @param systemModel {@link SystemModel} for storage
     * @param outputFile output file for storage
     * @throws IOException if storage is impossible
     */
    void storeSystemModel(SystemModel systemModel, File outputFile) throws IOException;

    /**
     * Load {@link SystemModel} from the file.
     *
     * @param inputFile file which stores {@link SystemModel}
     * @return system model
     * @throws IOException if loading is impossible
     */
    SystemModel loadSystemModel(File inputFile) throws IOException;

    /**
     * Store {@link UserRoleManagement} in the file.
     *
     * @param userRoleManagement {@link UserRoleManagement} for storage
     * @param outputFile output file for storage
     * @throws IOException if storage is impossible
     */
    void storeUserRoleManagement(UserRoleManagement userRoleManagement, File outputFile) throws IOException;

    /**
     * Load {@link UserRoleManagement} from the file.
     *
     * @param inputFile file which stores {@link UserRoleManagement}
     * @return user role management
     * @throws IOException if loading is impossible
     */
    UserRoleManagement loadUserRoleManagement(File inputFile) throws IOException;

    /**
     * Store {@link UnitManagement} in the file.
     *
     * @param unitManagement {@link UnitManagement} for storage
     * @param outputFile output file for storage
     * @throws IOException if storage is impossible
     */
    void storeUnitManagement(UnitManagement unitManagement, File outputFile) throws IOException;

    /**
     * Load {@link UnitManagement} from the file.
     *
     * @param inputStream inputStream which contains {@link UnitManagement}
     * @return unit management
     * @throws IOException if loading is impossible
     */
    UnitManagement loadUnitManagement(InputStream inputStream) throws IOException;

    /**
     * Store {@link Calculation} in the file.
     *
     * @param calculation {@link Calculation} for storage
     * @param outputFile output file for storage
     * @throws IOException if storage is impossible
     */
    void storeCalculation(Calculation calculation, File outputFile) throws IOException;

    /**
     * Load {@link Calculation} from the file.
     *
     * @param inputFile file which stores {@link Calculation}
     * @return calculation
     * @throws IOException if loading is impossible
     */
    Calculation loadCalculation(File inputFile) throws IOException;
}
