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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by d.knoll on 6/23/2017.
 */
@Entity
public class FigureOfMeritDefinition {

    @Id
    @Column(name = "id")
    @GeneratedValue
    private long id;

    private String name;

    private String unitOfMeasure;

    private String parameterModelLink;

    public FigureOfMeritDefinition(String name, String unitOfMeasure) {
        this.name = name;
        this.unitOfMeasure = unitOfMeasure;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParameterModelLink() {
        return parameterModelLink;
    }

    public void setParameterModelLink(String parameterModelLink) {
        this.parameterModelLink = parameterModelLink;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public static List<FigureOfMeritDefinition> buildFigureOfMeritDefinitions(String... names) {
        List<FigureOfMeritDefinition> definitions = new ArrayList<>(names.length);
        for (String name : names) {
            definitions.add(new FigureOfMeritDefinition(name, ""));
        }
        return definitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FigureOfMeritDefinition that = (FigureOfMeritDefinition) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return unitOfMeasure != null ? unitOfMeasure.equals(that.unitOfMeasure) : that.unitOfMeasure == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (unitOfMeasure != null ? unitOfMeasure.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FigureOfMeritDefinition{" +
                "name='" + name + '\'' +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                '}';
    }
}
