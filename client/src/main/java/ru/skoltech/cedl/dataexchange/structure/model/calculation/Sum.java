package ru.skoltech.cedl.dataexchange.structure.model.calculation;

/**
 * Created by D.Knoll on 23.09.2015.
 */
public class Sum implements Operation {

    @Override
    public String name() {
        return "Sum";
    }

    @Override
    public String description() {
        return "This operation sums all arguments";
    }

    @Override
    public String[] argumentNames() {
        return new String[]{"summand n"};
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return (o == null || getClass() != o.getClass());
    }

    public String toString() {
        return name();
    }
}
