/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model.calculation;

/**
 * Created by D.Knoll on 23.09.2015.
 */
public class Margin extends Operation {

    private static final String[] argNames = new String[]{"argument", "percentage"};

    @Override
    public String name() {
        return "Margin";
    }

    @Override
    public String description() {
        return "Adds a percentage of margin to the argument, given by the formula: argument * (1 + percentage)";
    }

    @Override
    public String argumentName(int index) {
        if (index < 0 || index >= argNames.length)
            throw new IllegalArgumentException("invalid argument index " + index);
        return argNames[index];
    }

    @Override
    public int minArguments() {
        return 2;
    }

    @Override
    public int maxArguments() {
        return 2;
    }

    @Override
    public double apply(double[] arguments) {
        return arguments[0] * (1 + arguments[1]);
    }
}
