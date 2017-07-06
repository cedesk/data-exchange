package ru.skoltech.cedl.dataexchange.repository;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

/**
 * Created by D.Knoll on 17.03.2015.
 */
public class StorageUtils {

    private static final Logger logger = Logger.getLogger(StorageUtils.class);

    private static final String APP_DIR = ".cedesk";

    private static final String USER_HOME_SYSTEM_PROPERTY_NAME = "user.home";
    private static final String APP_DIR_SYSTEM_PROPERTY_NAME = "cedesk.app.dir";
    private static final String DATA_DIR_SYSTEM_PROPERTY_NAME = "cedesk.data.dir";
    private static final String APP_START_TIME_SYSTEM_PROPERTY_NAME = "app.start.time";

    private static final String USER_HOME_SYSTEM_PROPERTY = System.getProperty(USER_HOME_SYSTEM_PROPERTY_NAME);
    private static final String APP_DIR_SYSTEM_PROPERTY = System.getProperty(APP_DIR_SYSTEM_PROPERTY_NAME);

    public static File getAppDir() {
        File appHome;
        if (APP_DIR_SYSTEM_PROPERTY == null) {
            File userHomeDir = new File(USER_HOME_SYSTEM_PROPERTY);
            appHome = new File(userHomeDir, APP_DIR);
        } else {
            appHome = new File(APP_DIR_SYSTEM_PROPERTY);
        }

        System.setProperty(DATA_DIR_SYSTEM_PROPERTY_NAME, appHome.getAbsolutePath()); // re-write in any case for log4j
        System.setProperty(APP_START_TIME_SYSTEM_PROPERTY_NAME, Utils.TIME_AND_DATE_FOR_FILENAMES.format(new Date()));

        if (!appHome.exists()) {
            boolean dirCreated = appHome.mkdirs();
            System.err.println("unable to create application directory in user home: " + appHome.getAbsolutePath());
        }
        return appHome;
    }

    public static File getDataDir(String repositoryUrl, String repositoryScheme, String projectName) {
        File repoDir = new File(getAppDir(), repositoryUrl);
        File schemaDir = new File(repoDir, repositoryScheme);
        return new File(schemaDir, projectName);
    }

    public static void makeDirectory(File path) {
        if (!path.exists()) {
            logger.info("Creating directory: " + path.toString());
            path.mkdirs();
        }
        if (!path.canRead() || !path.canWrite()) {
            logger.error("Warning: Directory is not usable: " + path.toString());
        }
    }

    public static boolean fileExistsAndIsNotEmpty(File file) {
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                if (br.readLine() == null) {
                    return false;
                }
            } catch (IOException e) {
                return false;
            }
            return true;
        }
        return false;
    }
}
