/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.entity.calculation.operation;

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
