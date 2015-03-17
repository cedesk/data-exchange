package ru.skoltech.cedl.dataexchange;

import java.io.*;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class Utils {

    public static InputStream getResourceAsStream(String fileName) {
        return Utils.class.getClassLoader().getResourceAsStream(fileName);
    }
}
