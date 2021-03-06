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

import org.apache.commons.lang3.ArrayUtils;
import ru.skoltech.cedl.dataexchange.Utils;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by d.knoll on 6/23/2017.
 */
@Entity
public class DesignPoint {

    @Id
    @Column(name = "id")
    @GeneratedValue
    private long id;

    private String description;

    @ManyToOne(targetEntity = Epoch.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Epoch epoch;

    @ManyToOne(targetEntity = ModelStateLink.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private ModelStateLink modelStateLink;

    @OneToMany(targetEntity = FigureOfMeritValue.class, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FigureOfMeritValue> values;

    @Transient
    private Map<FigureOfMeritDefinition, FigureOfMeritValue> valueMap;

    public DesignPoint(Epoch epoch, List<FigureOfMeritValue> values) {
        this.epoch = epoch;
        this.values = values;
    }

    public DesignPoint(String description, Epoch epoch, List<FigureOfMeritValue> values) {
        this.description = description;
        this.epoch = epoch;
        this.values = values;
    }

    public DesignPoint() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Epoch getEpoch() {
        return epoch;
    }

    public void setEpoch(Epoch epoch) {
        this.epoch = epoch;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ModelStateLink getModelStateLink() {
        return modelStateLink;
    }

    public void setModelStateLink(ModelStateLink modelStateLink) {
        this.modelStateLink = modelStateLink;
    }

    private Map<FigureOfMeritDefinition, FigureOfMeritValue> getValueMap() {
        if (valueMap == null) {
            this.valueMap = values.stream().collect(Collectors.toMap(FigureOfMeritValue::getDefinition, Function.identity()));
        }
        return valueMap;
    }

    public List<FigureOfMeritValue> getValues() {
        return values;
    }

    public void setValues(List<FigureOfMeritValue> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DesignPoint that = (DesignPoint) o;

        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (!epoch.equals(that.epoch)) return false;
        if (modelStateLink != null ? !modelStateLink.equals(that.modelStateLink) : that.modelStateLink != null)
            return false;
        return values.equals(that.values);
    }

    public String getFullDescription(FigureOfMeritDefinition... fomDefinitions) {
        List<String> fomTexts = new LinkedList<>();
        for (FigureOfMeritValue fomValue : values) {
            FigureOfMeritDefinition fomDefinition = fomValue.getDefinition();
            if (ArrayUtils.contains(fomDefinitions, fomDefinition)) {
                String formattedValue = Utils.NUMBER_FORMAT.format(fomValue.getValue());
                fomTexts.add(String.format("%s: %s (%s)", fomDefinition.getName(), formattedValue, fomDefinition.getUnitOfMeasure()));
            }
        }
        if (description != null && !description.trim().isEmpty()) {
            fomTexts.add(description);
        }
        return fomTexts.stream().collect(Collectors.joining(",\n"));
    }

    public FigureOfMeritValue getValue(FigureOfMeritDefinition figureOfMeritDefinition) {
        return getValueMap().get(figureOfMeritDefinition);
    }

    @Override
    public int hashCode() {
        int result = description != null ? description.hashCode() : 0;
        result = 31 * result + epoch.hashCode();
        result = 31 * result + (modelStateLink != null ? modelStateLink.hashCode() : 0);
        result = 31 * result + values.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DesignPoint{" +
                "description='" + description + '\'' +
                ", epoch=" + epoch +
                ", modelStateLink=" + modelStateLink +
                ", values=" + values +
                '}';
    }
}
