package ru.skoltech.cedl.dataexchange.units;

import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.units.model.Unit;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Created by D.Knoll on 29.08.2015.
 */
public class UnitAdapter extends XmlAdapter<String, Unit> {
    @Override
    public Unit unmarshal(String unitStr) throws Exception {
        UnitManagement unitManagement = ProjectContext.getInstance().getProject().getUnitManagement();
        Unit unit = unitManagement.findUnitByText(unitStr);
        return unit;
    }

    @Override
    public String marshal(Unit unit) throws Exception {
        return unit != null ? unit.asText() : "";
    }
}
