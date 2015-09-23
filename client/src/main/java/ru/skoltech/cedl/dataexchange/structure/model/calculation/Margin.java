package ru.skoltech.cedl.dataexchange.structure.model.calculation;

/**
 * Created by D.Knoll on 23.09.2015.
 */
public class Margin implements Operation {

    @Override
    public String name() {
        return "Margin";
    }

    @Override
    public String description() {
        return "Adds a percent of margin to the argument, given by the formula argument1 * (1 + argument2)";
    }

    @Override
    public String[] argumentNames() {
        return new String[]{"argument", "percentage"};
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return (o == null || getClass() != o.getClass());
    }

    @Override
    public String toString() {
        return name();
    }
}
