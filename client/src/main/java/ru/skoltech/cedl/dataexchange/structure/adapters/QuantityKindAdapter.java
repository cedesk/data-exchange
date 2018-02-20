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

import ru.skoltech.cedl.dataexchange.entity.unit.QuantityKind;
import ru.skoltech.cedl.dataexchange.repository.jpa.QuantityKindRepository;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by D.Knoll on 29.08.2015.
 */
public class QuantityKindAdapter extends XmlAdapter<String, QuantityKind> {

    private QuantityKindRepository quantityKindRepository;

    public QuantityKindAdapter(QuantityKindRepository quantityKindRepository) {
        this.quantityKindRepository = quantityKindRepository;
    }

    @Override
    public String marshal(QuantityKind quantityKind) {
        return quantityKind != null ? quantityKind.getName() : "";
    }

    @Override
    public QuantityKind unmarshal(String quantityKindStr) {
        return quantityKindRepository.findByName(quantityKindStr);
    }
}
