package ru.skoltech.cedl.dataexchange.structure.model.calculation;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Created by D.Knoll on 23.09.2015.
 */
@XmlRootElement
@XmlSeeAlso({Sum.class, Margin.class})
public interface Operation extends Comparable<Operation> {

    String name();

    String description();

    String[] argumentNames();

    int minArguments();

    int maxArguments();

    double apply(double[] argumentValues);

    @Override
    default int compareTo(Operation o) {
        return name().compareTo(o.name());
    }
}
