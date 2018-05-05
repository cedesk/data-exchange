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

package ru.skoltech.cedl.dataexchange.structure.adapters;

import org.apache.commons.lang3.tuple.Pair;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.List;

/**
 * {@link ExternalModel} adapter for JAXB marshalling and unmarshalling.
 * <p>
 * Created by Nikolay Groshkov on 26-Feb-18.
 */
public class AdaptedExternalModel extends ExternalModel {

    public static final String DEFAULT_TYPE = "EXCEL";

    @XmlAttribute
    private String type = DEFAULT_TYPE;

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public Double getValue(String target) {
        return null;
    }

    @Override
    public List<Double> getValues(List<String> targets) {
        return null;
    }

    @Override
    public void setValue(String target, Double value) {

    }

    @Override
    public void setValues(List<Pair<String, Double>> values) {

    }
}
