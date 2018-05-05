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

import org.springframework.beans.factory.annotation.Autowired;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.repository.jpa.UnitRepository;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * {@link Unit} adapter for JAXB marshalling and unmarshalling.
 *
 * Created by D.Knoll on 29.08.2015.
 */
public class UnitAdapter extends XmlAdapter<String, Unit> {

    @Autowired
    private UnitRepository unitRepository;

    @Override
    public String marshal(Unit unit) {
        return unit != null ? unit.asText() : "";
    }

    @Override
    public Unit unmarshal(String unitStr) {
        return unitRepository.findByText(unitStr);
    }
}
