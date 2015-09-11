package ru.skoltech.cedl.dataexchange.structure.model;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by D.Knoll on 08.09.2015.
 */
public class ParameterLinkRegistry {

    private Logger logger = Logger.getLogger(ParameterLinkRegistry.class);

    private Map<String, Set<ParameterModel>> valueLinks = new HashMap<>();

    public ParameterLinkRegistry() {
    }

    public void registerAllParameters(SystemModel systemModel) {
        clear();
        ParameterTreeIterator pmi = getLinkedParameters(systemModel);
        pmi.forEachRemaining(sink -> {
            addLink(sink.getValueLink(), sink);
        });
    }

    public ParameterTreeIterator getLinkedParameters(SystemModel systemModel) {
        return new ParameterTreeIterator(systemModel,
                sink -> sink.getNature() == ParameterNature.INPUT &&
                        sink.getValueSource() == ParameterValueSource.LINK &&
                        sink.getValueLink() != null);
    }

    public void addLink(ParameterModel source, ParameterModel sink) {
        logger.debug("sink '" + sink.getNodePath() + "' is linking to source '" + source.getNodePath() + "'");
        String sourceId = source.getUuid();
        if (valueLinks.containsKey(sourceId)) {
            valueLinks.get(sourceId).add(sink);
        } else {
            Set<ParameterModel> sinks = new TreeSet<>();
            sinks.add(sink);
            valueLinks.put(sourceId, sinks);
        }
    }

    public void updateAll(SystemModel systemModel) {
        ParameterTreeIterator pmi = getLinkedParameters(systemModel);
        pmi.forEachRemaining(sink -> {
            updateSinks(sink.getValueLink());
        });
    }

    public void updateSinks(ParameterModel source) {
        String sourceId = source.getUuid();
        if (valueLinks.containsKey(sourceId)) {
            Set<ParameterModel> parameterModels = valueLinks.get(sourceId);
            for (ParameterModel parameterModel : parameterModels) {
                if (parameterModel.getValueLink() == source) {
                    logger.error("updating sink '" + parameterModel.getNodePath() + "' from source '" + source.getNodePath() + "'");
                    parameterModel.setValue(source.getValue());
                    parameterModel.setUnit(source.getUnit());
                    // TODO: notify UI ?
                } else {
                    logger.error("model changed, sink '" + parameterModel.getNodePath() + "' no longer referencing '" + source.getNodePath() + "'");
                }
            }
        }
    }

    public void removeLink(ParameterModel source, ParameterModel sink) {
        String sourceId = source.getUuid();
        if (valueLinks.containsKey(sourceId)) {
            Set<ParameterModel> sinks = valueLinks.get(sourceId);
            sinks.remove(sink);
            if (sinks.isEmpty()) {
                valueLinks.remove(sourceId);
            }
        }
    }

    public void clear() {
        valueLinks.clear();
    }
}
