package ru.skoltech.cedl.dataexchange;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class Utils {

    public static final DateFormat TIME_AND_DATE_FOR_FILENAMES = new SimpleDateFormat("yyyy-MM-dd_HH-mm");

    public static final DateFormat TIME_AND_DATE_FOR_USER_INTERFACE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getUserName() {
        String userName = System.getProperty("user.name");
        return userName.toLowerCase();
    }

    public static String getFormattedDateAndTime() {
        Date now = new Date();
        return TIME_AND_DATE_FOR_FILENAMES.format(now);
    }

    public static <T extends Comparable<T>> Set<T> symmetricDiffTwoLists(List<T> l1,
                                                                         List<T> l2) {
        Set<T> symDiff = new TreeSet<T>(Comparator.<T>naturalOrder());
        symDiff.addAll(l1);
        symDiff.addAll(l2);
        Set<T> intersection = new TreeSet<>(Comparator.<T>naturalOrder());
        intersection.addAll(l1);
        intersection.retainAll(l2);
        symDiff.removeAll(intersection);
        return symDiff;
    }
}
