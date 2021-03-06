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

package ru.skoltech.cedl.dataexchange;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.logging.Logger;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class Utils {

    public static final long INVALID_TIME = -1;

    public static final DateFormat TIME_AND_DATE_FOR_FILENAMES = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
    public static final DateFormat TIME_AND_DATE_FOR_USER_INTERFACE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("###0.0##");
    private static final Logger logger = Logger.getLogger(Utils.class);

    public static String getFormattedDateAndTime() {
        Date now = new Date();
        return TIME_AND_DATE_FOR_FILENAMES.format(now);
    }

    public static String getFullHostname() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName().toLowerCase();
        } catch (UnknownHostException e) {
            // ignore
        }
        return "localhost.localdomain";
    }

    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName().toLowerCase();
        } catch (UnknownHostException e) {
            // ignore
        }
        return "localhost";
    }

    /**
     * Compares two version strings.
     * <p>
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @param ver1 a string of ordinal numbers separated by decimal points.
     * @param ver2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if * is _numerically_ less than ver2.
     * The result is a positive integer if * is _numerically_ greater than ver2.
     * The result is zero if the strings are _numerically_ equal.
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     */
    public static int compareVersions(String ver1, String ver2) {
        String[] str1 = ver1.split("-");
        String[] str2 = ver2.split("-");
        String[] vals1 = str1[0].split("\\.");
        String[] vals2 = str2[0].split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        else if (str1.length == 1 && str2.length == 1) {
            return Integer.signum(vals1.length - vals2.length);
        }
        // "1.13-snapshot" < "1.13"
        else {
            if (str1.length == 2 && str2.length == 1) return -1;
            if (str1.length == 1 && str2.length == 2) return 1;
            return str1[1].compareTo(str2[1]);
        }
    }

    public static <T> T copyBean(T source, T target) {
        try {
            PropertyUtils.copyProperties(target, source);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("error copying bean " + source.toString(), e);
        }
        return target;
    }

    static void writeToFile(Serializable object, File file) {
        logger.info("writing to file: " + file.getAbsolutePath());
        try {
            ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(file));
            os.writeObject(object);
            os.close();
        } catch (Exception e) {
            logger.error("failed writing OBJ file", e);
        }
    }

    static Object readFromFile(File file) {
        logger.info("reading from file: " + file.getAbsolutePath());
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            Object object = ois.readObject();
            ois.close();
            return object;
        } catch (Exception e) {
            logger.error("failed read OBJ file", e);
        }
        return null;
    }
}
