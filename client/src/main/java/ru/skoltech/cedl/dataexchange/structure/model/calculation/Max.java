package ru.skoltech.cedl.dataexchange.structure.model.calculation;

import java.util.Arrays;
import java.util.OptionalDouble;

/**
 * Created by D.Knoll on 26.09.2015.
 */
public class Max extends Operation {

    @Override
    public String name() {
        return "Max";
    }

    @Override
    public String description() {
        return "This operation finds the maximum among all arguments";
    }

    @Override
    public String argumentName(int index) {
        if (index < 0)
            throw new IllegalArgumentException("invalid argument index " + index);
        return "argument " + index;
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
        OptionalDouble optionalDouble = Arrays.stream(arguments).max();
        return optionalDouble.getAsDouble();
    }
}
