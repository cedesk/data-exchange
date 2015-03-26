package ru.skoltech.cedl.dataexchange;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by D.Knoll on 12.03.2015.
 */
public class Utils {

    public static InputStream getResourceAsStream(String fileName) {
        return Utils.class.getClassLoader().getResourceAsStream(fileName);
    }

    public static <T> Set<T> symmetricDiffTwoLists(List<T> l1,
                                                   List<T> l2) {
        Set<T> s1 = new HashSet<>(l1);
        Set<T> s2 = new HashSet<>(l2);

        Set<T> symDiff = new HashSet<>(s1);
        symDiff.addAll(s2);
        Set<T> tmpSet = new HashSet<>(s1);
        tmpSet.retainAll(s2);
        symDiff.removeAll(tmpSet);
        return symDiff;
    }
}
