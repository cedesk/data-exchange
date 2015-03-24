package ru.skoltech.cedl.dataexchange.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by D.Knoll on 17.03.2015.
 */
public class StorageUtils {

    private static final String MODEL_FILE = "cedesk-system-model.xml";

    private static final String REVISION_FILE = "revision.txt";

    private static final String DATA_DIR = "CEDESK";

    private static final String CO_DIR = ".co";
    private static final String USER_HOME = System.getProperty("user.home");

    private static File getDataDir() {
        File homeDir = new File(USER_HOME);
        return new File(homeDir, DATA_DIR);
    }

    public static File getDataFile() {
        File dataFile = new File(getDataDir(), MODEL_FILE);
        if (!dataFile.exists()) {
            System.err.println("Warning: Data file does not exist!");
        } else if (!dataFile.canRead() || !dataFile.canWrite()) {
            System.err.println("Warning: Data file is not usable!");
        }
        return dataFile;
    }

    public static File getCheckedoutDataFile() {
        File checkedoutDir = new File(getDataDir(), CO_DIR);
        makeDirectory(checkedoutDir);
        File dataFile = new File(checkedoutDir, MODEL_FILE);
        if (!dataFile.exists()) {
            System.err.println("Warning: Data file does not exist!");
        } else if (!dataFile.canRead() || !dataFile.canWrite()) {
            System.err.println("Warning: Data file is not usable!");
        }
        return dataFile;
    }

    public static File getWorkingCopyDirectory() {
        File dataDir = new File(getDataDir(), CO_DIR);
        makeDirectory(dataDir);
        if (!dataDir.exists()) {
            System.err.println("Warning: Data file does not exist!");
        } else if (!dataDir.canRead() || !dataDir.canWrite()) {
            System.err.println("Warning: Data file is not usable!");
        }
        return dataDir;
    }

    public static File getCheckedoutRivisionFile() {
        File checkedoutDir = new File(getDataDir(), CO_DIR);
        makeDirectory(checkedoutDir);
        File dataFile = new File(checkedoutDir, REVISION_FILE);
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

    public static void makeWorkingDirectory() {
        File dataDir = getDataDir();
        makeDirectory(dataDir);
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
