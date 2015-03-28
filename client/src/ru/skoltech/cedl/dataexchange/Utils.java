package ru.skoltech.cedl.dataexchange;

import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

import java.io.InputStream;
import java.util.*;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class Utils {

    public static InputStream getResourceAsStream(String fileName) {
        return Utils.class.getClassLoader().getResourceAsStream(fileName);
    }

    public static String getUserName() {
        String userName = System.getProperty("user.name");
        return userName;
    }

    public static <T extends Comparable<T>> Set<T> symmetricDiffTwoLists(List<T> l1,
                                                   List<T> l2) {
        Set<T> symDiff = new TreeSet<T>(Comparator.<T >naturalOrder());
        symDiff.addAll(l1);
        symDiff.addAll(l2);
        Set<T> intersection = new TreeSet<>(Comparator.<T>naturalOrder());
        intersection.addAll(l1);
        intersection.retainAll(l2);
        symDiff.removeAll(intersection);
        return symDiff;
    }
}
