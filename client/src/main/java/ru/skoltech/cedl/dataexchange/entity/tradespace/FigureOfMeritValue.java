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

package ru.skoltech.cedl.dataexchange.entity.tradespace;

import javax.persistence.*;

/**
 * Created by d.knoll on 6/23/2017.
 */
@Entity
public class FigureOfMeritValue {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private long id;

    @ManyToOne(targetEntity = FigureOfMeritDefinition.class)
    private FigureOfMeritDefinition definition;

    private Double value;

    public FigureOfMeritValue(FigureOfMeritDefinition definition, Double value) {
        this.definition = definition;
        this.value = value;
    }

    public FigureOfMeritDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(FigureOfMeritDefinition definition) {
        this.definition = definition;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FigureOfMeritValue that = (FigureOfMeritValue) o;

        if (!definition.equals(that.definition)) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = definition.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FigureOfMeritValue{" +
                "definition=" + definition +
                ", value=" + value +
                '}';
    }
}
