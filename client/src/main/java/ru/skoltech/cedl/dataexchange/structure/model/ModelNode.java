package ru.skoltech.cedl.dataexchange.structure.model;

import ru.skoltech.cedl.dataexchange.Utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 11.03.2015.
 */
public abstract class ModelNode {

    private String name;

    private List<ParameterModel> parameters = new LinkedList<>();


    public ModelNode() {
    }

    public ModelNode(String name) {
        this.name = name;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParameters(List<ParameterModel> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(ParameterModel parameter) {
        parameters.add(parameter);
    }

    public List<ParameterModel> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return getName();
    }

    public void initializeServerValues(ModelNode modelNode) {
        List<ParameterModel> thisParams = getParameters();
        List<ParameterModel> l = modelNode.getParameters();

        Set<ParameterModel> diff = Utils.symmetricDiffTwoLists(thisParams, l);

        Map<String, ParameterModel> map1 = thisParams.stream().collect(
                Collectors.toMap(ParameterModel::getName, (m) -> m)
        );

        Map<String, ParameterModel> map2 = l.stream().collect(
                Collectors.toMap(ParameterModel::getName, (m) -> m)
        );

        Iterator<ParameterModel> i = diff.iterator();
        while (i.hasNext()) {
            ParameterModel param = i.next();
            String n = param.getName();
            ParameterModel diffParam = null;
            if (map1.containsKey(n) &&
                    map2.containsKey(n)) {
                diffParam = map1.get(n);
                diffParam.setServerValue(map2.get(n).getValue());
            } else if (map1.containsKey(n)) {
                // TODO: This parameter needs to be removed from the model after the user exits the diff view.
                diffParam = map1.get(n);
                diffParam.setServerValue(null);
            } else if (map2.containsKey(n)) {
                diffParam = new ParameterModel(map2.get(n).getName(), null);
                diffParam.setServerValue(map2.get(n).getValue());
                addParameter(diffParam);
            }
        }
    }
}
