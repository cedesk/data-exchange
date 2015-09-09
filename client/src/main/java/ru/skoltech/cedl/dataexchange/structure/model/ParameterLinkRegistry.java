package ru.skoltech.cedl.dataexchange.structure.model;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by D.Knoll on 08.09.2015.
 */
public class ParameterLinkRegistry {

    private Logger logger = Logger.getLogger(ParameterLinkRegistry.class);

    private Map<String, List<ParameterModel>> valueLinks = new HashMap<>();

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
        logger.debug("source '" + source.getNodePath() + "' is now linked by sink '" + sink.getNodePath() + "'");
        String sourceId = source.getNodePath();
        if (valueLinks.containsKey(sourceId)) {
            valueLinks.get(sourceId).add(sink);
        } else {
            List<ParameterModel> sinks = new LinkedList<>();
            sinks.add(sink);
            valueLinks.put(sourceId, sinks);
        }
    }

    public void updateAll(SystemModel systemModel) {
        ParameterTreeIterator pmi = new ParameterTreeIterator(systemModel,
                sink -> sink.getNature() == ParameterNature.INPUT &&
                        sink.getValueSource() == ParameterValueSource.LINK &&
                        sink.getValueLink() != null);
        pmi.forEachRemaining(sink -> {
            updateSinks(sink.getValueLink());
        });
        //valueLinks.keySet().forEach(this::updateSinks);
    }

    public void updateSinks(ParameterModel source) {
        String sourceId = source.getNodePath();
        if (valueLinks.containsKey(sourceId)) {
            List<ParameterModel> parameterModels = valueLinks.get(sourceId);
            for (ParameterModel parameterModel : parameterModels) {
                if (parameterModel.getValueLink() == source) {
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
        String sourceId = source.getNodePath();
        if (valueLinks.containsKey(sourceId)) {
            List<ParameterModel> sinks = valueLinks.get(sourceId);
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
