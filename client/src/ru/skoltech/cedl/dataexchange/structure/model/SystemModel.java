package ru.skoltech.cedl.dataexchange.structure.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 11.03.2015.
 */
@XmlRootElement
public class SystemModel extends CompositeModelNode<SubSystemModel> {

    public SystemModel() {
        super();
    }

    public SystemModel(String name) {
        super(name);
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

    @Override
    public void initializeServerValues(ModelNode modelNode) {
        super.initializeServerValues(modelNode);

        SystemModel systemModel = (SystemModel)modelNode;

        if (systemModel == null) {
            return;
        }
        Iterator<SubSystemModel> i = iterator();
        while (i.hasNext()) {
            SubSystemModel subSystemModel = i.next();
            List<SubSystemModel> subSystemModels = systemModel.getSubSystems();

            Map<String, SubSystemModel> map1 = subSystemModels.stream().collect(
                    Collectors.toMap(SubSystemModel::getName, (m) -> m)
            );

            String n = subSystemModel.getName();
            if (map1.containsKey(n)) {
                SubSystemModel compareTo = map1.get(n);
                subSystemModel.initializeServerValues(compareTo);
            }
        }
    }

}
