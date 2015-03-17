package ru.skoltech.cedl.dataexchange;

import java.io.*;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class Utils {

    public static InputStream getResourceAsStream(String fileName) {
        return Utils.class.getClassLoader().getResourceAsStream(fileName);
    }

    public static boolean fileExistsAndIsNotEmpty(String fileName) {

        File file = new File(fileName);
        if (file.exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
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
