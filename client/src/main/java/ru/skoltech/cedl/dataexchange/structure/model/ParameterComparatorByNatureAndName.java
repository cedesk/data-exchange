/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model;

import java.util.Comparator;

/**
 * Created by d.knoll on 24.06.2016.
 */
public class ParameterComparatorByNatureAndName implements Comparator<ParameterModel> {
    /**
     * The comparison is first based on the parameterNature and then on the name fields.
     */
    @Override
    public int compare(ParameterModel o1, ParameterModel o2) {
        int natureCompare = o1.getNature().compareTo(o2.getNature());
        if (natureCompare != 0) return natureCompare;
        return o1.getName().compareTo(o2.getName());
    }
}
