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

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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
public class MultitemporalTradespace {

    @Id
    @Column(name = "id")
    private long id;

    @OneToMany(targetEntity = Epoch.class, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "mts_id", referencedColumnName = "id")
    @Fetch(FetchMode.SELECT)
    private List<Epoch> epochs = new LinkedList<>();

    @OneToMany(targetEntity = FigureOfMeritDefinition.class, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "mts_id", referencedColumnName = "id")
    @Fetch(FetchMode.SELECT)
    private List<FigureOfMeritDefinition> definitions = new LinkedList<>();


    @OneToMany(targetEntity = DesignPoint.class, cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "mts_id", referencedColumnName = "id")
    @Fetch(FetchMode.SELECT)
    private List<DesignPoint> designPoints = new LinkedList<>();


    public MultitemporalTradespace() {
    }

    public List<FigureOfMeritDefinition> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(List<FigureOfMeritDefinition> definitions) {
        this.definitions = definitions;
    }

    public Map<String, FigureOfMeritDefinition> getDefinitionsMap() {
        return definitions.stream().collect(Collectors.toMap(FigureOfMeritDefinition::getName, Function.identity()));
    }

    public List<DesignPoint> getDesignPoints() {
        return designPoints;
    }

    public void setDesignPoints(List<DesignPoint> designPoints) {
        this.designPoints = designPoints;
    }

    public List<Epoch> getEpochs() {
        return epochs;
    }

    public String getEpochsFormatted() {
        return epochs.stream().map(Epoch::asText).collect(Collectors.joining(", "));
    }

    public void setEpochs(List<Epoch> epochs) {
        this.epochs = epochs;
    }

    public long getId() {
        return id;
    }

    public void setId(Long studyId) {
        this.id = studyId;
    }

    public Epoch getEpoch(int index) {
        return epochs.get(index);
    }

    @Override
    public String toString() {
        return "MultitemporalTradespace{" +
                "id=" + id +
                ", epochs=" + epochs +
                ", definitions=" + definitions +
                ", designPoints=" + designPoints +
                '}';
    }
}