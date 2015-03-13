package ru.skoltech.cedl.dataexchange.structure.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by D.Knoll on 11.03.2015.
 */
@XmlRootElement
public class SystemModel extends ModelNode {

    private List<SubSystemModel> subSystems;

    public SystemModel() {
        super();
    }

    public SystemModel(String name) {
        super(name);
        subSystems = new LinkedList<>();
    }

    public List<SubSystemModel> getSubSystems() {
        return subSystems;
    }

    public void setSubSystems(List<SubSystemModel> subSystems) {
        this.subSystems = subSystems;
    }

    public boolean addSubsystem(SubSystemModel subSystem) {
        return subSystems.add(subSystem);
    }

    public Iterator<SubSystemModel> iterator() {
        return subSystems.iterator();
    }


}
