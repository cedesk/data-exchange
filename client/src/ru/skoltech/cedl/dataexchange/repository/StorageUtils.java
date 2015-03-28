package ru.skoltech.cedl.dataexchange.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by D.Knoll on 17.03.2015.
 */
public class StorageUtils {

    private static final String DATA_DIR = "CEDESK";

    private static final String MODEL_FILE = "cedesk-system-model.xml";

    private static final String USER_HOME = System.getProperty("user.home");

    public static File getDataDir(String projectName) {
        File homeDir = new File(USER_HOME);
        File dataDir = new File(homeDir, DATA_DIR);
        return new File(dataDir, projectName);
    }

    public static File getDataFile(String projectName) {
        File dataFile = new File(getDataDir(projectName), MODEL_FILE);
        if (!dataFile.exists()) {
            System.err.println("Warning: Data file does not exist!");
        } else if (!dataFile.canRead() || !dataFile.canWrite()) {
            System.err.println("Warning: Data file is not usable!");
        }
        return dataFile;
    }

    public static void makeDirectory(File path) {
        if (!path.exists()) {
            System.out.println("Creating directory: " + path.toString());
            path.mkdirs();
        }
        if (!path.canRead() || !path.canWrite()) {
            System.err.println("Warning: Directory is not usable: " + path.toString());
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

    public static String getDataFileName() {
        return MODEL_FILE;
    }
}
