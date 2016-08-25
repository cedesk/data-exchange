package ru.skoltech.cedl.dataexchange.repository;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by D.Knoll on 17.03.2015.
 */
public class StorageUtils {

    private static final Logger logger = Logger.getLogger(StorageUtils.class);

    private static final String APP_DIR = ".cedesk";

    private static final String USER_HOME = System.getProperty("user.home");

    public static File getAppDir() {
        File appHome;
        String cedeskAppDirProperty = System.getProperty("cedesk.app.dir");
        if (cedeskAppDirProperty == null) {
            File homeDir = new File(USER_HOME);
            appHome = new File(homeDir, APP_DIR);
        } else {
            appHome = new File(cedeskAppDirProperty);
        }
        cedeskAppDirProperty = appHome.getAbsolutePath();
        System.setProperty("cedesk.data.dir", cedeskAppDirProperty); // re-write in any case for log4j

        if (!appHome.exists()) {
            boolean dirCreated = appHome.mkdirs();
            System.err.println("unable to create application directory in user home: " + cedeskAppDirProperty);
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
