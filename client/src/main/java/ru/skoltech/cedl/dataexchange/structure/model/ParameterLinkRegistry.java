package ru.skoltech.cedl.dataexchange.structure.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by D.Knoll on 08.09.2015.
 */
public class ParameterLinkRegistry {

    private Map<ParameterModel, List<ParameterModel>> valueLinks = new HashMap<>();

    public ParameterLinkRegistry() {
    }

    public void registerAllParameters(SystemModel systemModel) {
        clear();
        ParameterTreeIterator pmi = new ParameterTreeIterator(systemModel,
                sink -> sink.getNature() == ParameterNature.INPUT &&
                        sink.getValueSource() == ParameterValueSource.LINK &&
                        sink.getValueLink() != null);
        pmi.forEachRemaining(sink -> {
            addLink(sink.getValueLink(), sink);
        });
    }

    public void addLink(ParameterModel source, ParameterModel sink) {
        if (valueLinks.containsKey(source)) {
            valueLinks.get(source).add(sink);
        } else {
            List<ParameterModel> sinks = new LinkedList<>();
            sinks.add(sink);
            valueLinks.put(source, sinks);
        }
    }

    public void updateAll() {
        valueLinks.keySet().forEach(this::updateSinks);
    }

    public void updateSinks(ParameterModel source) {
        if (valueLinks.containsKey(source)) {
            List<ParameterModel> parameterModels = valueLinks.get(source);
            for (ParameterModel parameterModel : parameterModels) {
                parameterModel.setValue(source.getValue());
                parameterModel.setUnit(source.getUnit());
                // TODO: notify UI ?
            }
        }
    }

    public void removeLink(ParameterModel source, ParameterModel sink) {
        if (valueLinks.containsKey(source)) {
            valueLinks.get(source).remove(sink);
        }
    }

    public void clear() {
        valueLinks.clear();
    }
}
