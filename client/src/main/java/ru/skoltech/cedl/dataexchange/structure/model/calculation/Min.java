package ru.skoltech.cedl.dataexchange.structure.model.calculation;

import java.util.Arrays;
import java.util.OptionalDouble;

/**
 * Created by D.Knoll on 26.09.2015.
 */
public class Min extends Operation {

    @Override
    public String name() {
        return "Min";
    }

    @Override
    public String description() {
        return "This operation finds the minimum among all arguments";
    }

    @Override
    public String[] argumentNames() {
        return new String[]{"argument n"};
    }

    @Override
    public int minArguments() {
        return 2;
    }

    @Override
    public int maxArguments() {
        return Integer.MAX_VALUE;
    }

    @Override
    public double apply(double[] arguments) {
        OptionalDouble optionalDouble = Arrays.stream(arguments).min();
        return optionalDouble.getAsDouble();
    }
}
