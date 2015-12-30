package ru.skoltech.cedl.dataexchange.structure.model.calculation;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Created by D.Knoll on 23.09.2015.
 */
@XmlRootElement
@XmlSeeAlso({Sum.class, Margin.class, Min.class, Max.class})
public abstract class Operation implements Comparable<Operation> {

    public abstract String name();

    public abstract String description();

    public abstract String argumentName(int index);

    public abstract int minArguments();

    public abstract int maxArguments();

    public abstract double apply(double[] argumentValues);

    @Override
    public int compareTo(Operation o) {
        return name().compareTo(o.name());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return (o != null && getClass() == o.getClass());
    }

    @Override
    public String toString() {
        return name();
    }
}
