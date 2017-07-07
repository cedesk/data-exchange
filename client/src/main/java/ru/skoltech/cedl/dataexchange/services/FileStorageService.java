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
 * Created by n.groshkov on 06-Jul-17.
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
     * @param systemModel
     * @param outputFile
     * @throws IOException
     */
    void storeSystemModel(SystemModel systemModel, File outputFile) throws IOException;

    /**
     * Load {@link SystemModel} from the file.
     *
     * @param inputFile
     * @return system model
     * @throws IOException
     */
    SystemModel loadSystemModel(File inputFile) throws IOException;

    /**
     * Store {@link UserRoleManagement} in the file.
     *
     * @param userRoleManagement
     * @param outputFile
     * @throws IOException
     */
    void storeUserRoleManagement(UserRoleManagement userRoleManagement, File outputFile) throws IOException;

    /**
     * Load {@link UserRoleManagement} from the file.
     *
     * @param inputFile
     * @return user role management
     * @throws IOException
     */
    UserRoleManagement loadUserRoleManagement(File inputFile) throws IOException;

    /**
     * Store {@link UnitManagement} in the file.
     *
     * @param unitManagement
     * @param outputFile
     * @throws IOException
     */
    void storeUnitManagement(UnitManagement unitManagement, File outputFile) throws IOException;

    /**
     * Load {@link UnitManagement} from the file.
     *
     * @param inputStream
     * @return unit management
     * @throws IOException
     */
    UnitManagement loadUnitManagement(InputStream inputStream) throws IOException;

    /**
     * Store {@link Calculation} in the file.
     *
     * @param calculation
     * @param outputFile
     * @throws IOException
     */
    void storeCalculation(Calculation calculation, File outputFile) throws IOException;

    /**
     * Load {@link Calculation} from the file.
     *
     * @param file
     * @return calculation
     * @throws IOException
     */
    Calculation loadCalculation(File file) throws IOException;
}
