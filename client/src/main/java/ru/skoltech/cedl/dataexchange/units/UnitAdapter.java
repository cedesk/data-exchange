/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.units;

import org.springframework.context.ApplicationContext;
import ru.skoltech.cedl.dataexchange.ApplicationContextInitializer;
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
        UnitManagement unitManagement = context.getBean(Project.class).getUnitManagement();
        Unit unit = unitManagement.findUnitByText(unitStr);
        return unit;
    }

    @Override
    public String marshal(Unit unit) throws Exception {
        return unit != null ? unit.asText() : "";
    }
}
