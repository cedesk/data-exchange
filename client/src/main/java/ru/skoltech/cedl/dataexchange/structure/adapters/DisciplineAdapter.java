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

import ru.skoltech.cedl.dataexchange.entity.user.Discipline;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by Nikolay Groshkov on 27-Nov-17.
 */
public class DisciplineAdapter extends XmlAdapter<String, Discipline> {

    @Override
    public String marshal(Discipline discipline) throws Exception {
        if (discipline == null) {
            return null;
        }
        return discipline.getName();
    }

    @Override
    public Discipline unmarshal(String name) throws Exception {
        Discipline entity = new Discipline();
        entity.setName(name);
        return entity;
    }
}
