/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * Created by D.Knoll on 28.12.2016.
 */
public class EnumUtil {

    private final static HashMap<Class, Object[]> ordinalCache = new HashMap<>();
    private final static HashMap<Class, HashMap<String, Enum>> nameCache = new HashMap<>();

    /**
     * convert ordinal to Enum
     *
     * @param enumClass may not be null
     * @param ordinal
     * @return e with e.ordinal( ) == ordinal
     * @throws IllegalArgumentException if ordinal out of range
     */
    public static <E extends Enum<E>> E lookupEnum(Class<E> enumClass, Number ordinal) {
        if (ordinal == null) return null;
        int ord = ordinal.intValue();
        if (!ordinalCache.containsKey(enumClass)) {
            EnumSet<E> set = EnumSet.allOf(enumClass);
            Object[] values = new Object[set.size()];
            for (E rval : set) {
                values[rval.ordinal()] = rval;
            }
            ordinalCache.put(enumClass, values);
        }
        Object[] enums = ordinalCache.get(enumClass);
        if (ord >= 0 && ord < enums.length) {
            return (E) enums[ord];
        }
        throw new IllegalArgumentException("Invalid value " + ordinal + " for " + enumClass.getName() + ", must be < " + enums.length);
    }

    /**
     * convert name to Enum
     *
     * @param enumClass may not be null
     * @param name
     * @return e with e.name( ) equals name
     * @throws IllegalArgumentException if ordinal out of range
     */
    public static <E extends Enum<E>> E lookupEnum(Class<E> enumClass, String name) {
        if (name == null) return null;
        name = name.toLowerCase();
        if (!nameCache.containsKey(enumClass)) {
            EnumSet<E> set = EnumSet.allOf(enumClass);
            HashMap<String, Enum> values = new HashMap<>();
            for (E rval : set) {
                values.put(rval.name().toLowerCase(), rval);
            }
            nameCache.put(enumClass, values);
        }
        HashMap<String, Enum> enums = nameCache.get(enumClass);
        if (enums.containsKey(name)) {
            return (E) enums.get(name);
        }
        throw new IllegalArgumentException("Invalid value " + name + " for " + enumClass.getName());
    }

}