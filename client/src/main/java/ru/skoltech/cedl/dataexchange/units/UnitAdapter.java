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

package ru.skoltech.cedl.dataexchange.units;

import org.springframework.context.ApplicationContext;
import ru.skoltech.cedl.dataexchange.ApplicationContextInitializer;
import ru.skoltech.cedl.dataexchange.services.UnitManagementService;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.units.model.Unit;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by D.Knoll on 29.08.2015.
 */
public class UnitAdapter extends XmlAdapter<String, Unit> {
    @Override
    public Unit unmarshal(String unitStr) throws Exception {
        // TODO: rewrite for proper injection
        ApplicationContext context = ApplicationContextInitializer.getInstance().getContext();
        Project project = context.getBean(Project.class);
        UnitManagementService unitManagementService = context.getBean(UnitManagementService.class);
        UnitManagement unitManagement = project.getUnitManagement();
        return unitManagementService.obtainUnitByText(unitManagement, unitStr);
    }

    @Override
    public String marshal(Unit unit) throws Exception {
        return unit != null ? unit.asText() : "";
    }
}
