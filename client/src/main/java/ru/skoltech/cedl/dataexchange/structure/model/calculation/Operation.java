/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model.calculation;

import org.hibernate.annotations.Immutable;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.io.Serializable;

/**
 * Created by D.Knoll on 23.09.2015.
 */

@MappedSuperclass
@Immutable
@XmlRootElement
@XmlSeeAlso({Sum.class, Margin.class, Min.class, Max.class})
public abstract class Operation implements Comparable<Operation>, Serializable {

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
