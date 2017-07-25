/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.analytics;

import org.apache.commons.lang3.tuple.Triple;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by D.Knoll on 03.12.2016.
 */
public class NumericalDSM {
    private List<String> elementNamesList = new LinkedList<>();
    private List<Triple<Integer, Integer, Float>> linkList = new LinkedList<>();

    void addElementName(String name) {
        elementNamesList.add(name);
    }

    void addLink(int to, int from, float weight) {
        linkList.add(Triple.of(to, from, weight));
    }

    private String getElementNames() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < elementNamesList.size(); i++) {
            sb.append(String.format("DSMLABEL{%d,1} = '%s';", i + 1, elementNamesList.get(i)));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String getLinkMatrix(boolean weighted) {
        StringBuilder sb = new StringBuilder();
        for (Triple<Integer, Integer, Float> link : linkList) {
            Float weight = weighted ? link.getRight() : Float.valueOf(1);
            sb.append(String.format(Locale.ENGLISH, "DSM(%d,%d) = %f;", link.getLeft(), link.getMiddle(), weight));
            sb.append("\n");
        }
        return sb.toString();
    }

    public String getMatlabCode(boolean weighted) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DSM_size = %d;\n", elementNamesList.size()));
        sb.append("DSMLABEL = cell(DSM_size,1);\n\n");
        sb.append(getElementNames());
        sb.append("\nDSM = zeros(DSM_size);\n\n");
        sb.append(getLinkMatrix(weighted));
        return sb.toString();
    }
}
