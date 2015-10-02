package ru.skoltech.cedl.dataexchange.structure.model.calculation;

/**
 * Created by D.Knoll on 23.09.2015.
 */
public class Sum extends Operation {

    @Override
    public String name() {
        return "Sum";
    }

    @Override
    public String description() {
        return "This operation sums all arguments";
    }

    @Override
    public String argumentName(int index) {
        if (index < 0)
            throw new IllegalArgumentException("invalid argument index " + index);
        return "summand " + index;
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
        double result = 0;
        for (Double arg : arguments) {
            result += arg;
        }
        return result;
    }
}
