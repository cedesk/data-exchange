package ru.skoltech.cedl.dataexchange.structure.model;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.*;
import java.util.stream.Collectors;

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
        DirectedGraph<ModelNode, MyEdge> modelDependencies = calculateModelDependencies(systemModel);
        printDependencies(modelDependencies);
    }

    public ParameterTreeIterator getLinkedParameters(SystemModel systemModel) {
        Objects.requireNonNull(systemModel);
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

    private SystemModel getSystem() {
        ModelNode parent = null;
        if (valueLinks.size() > 0) {
            Set<ParameterModel> sinks = valueLinks.values().iterator().next();
            ParameterModel sink = sinks.iterator().next();
            parent = sink.getParent();
        }
        SystemModel result = null;
        if (parent != null) {
            while (parent.getParent() != null) {
                parent = parent.getParent();
            }
            result = (SystemModel) parent;
        }
        return result;
    }

    public DirectedGraph<ModelNode, MyEdge> calculateModelDependencies(SystemModel systemModel) {
        DirectedGraph<ModelNode, MyEdge> dependencies = new SimpleDirectedGraph<ModelNode, MyEdge>(MyEdge.class);

        ParameterTreeIterator pmi = getLinkedParameters(systemModel);
        pmi.forEachRemaining(valueSink -> {
            ModelNode from = valueSink.getParent();
            ParameterModel valueSource = valueSink.getValueLink();
            ModelNode to = valueSource.getParent();
            dependencies.addVertex(from);
            dependencies.addVertex(to);
            dependencies.addEdge(from, to);
        });
        return dependencies;
    }

    private void printDependencies(DirectedGraph<ModelNode, MyEdge> modelDependencies) {
        logger.info("DEPENDENCIES");
        modelDependencies.vertexSet().stream()
                .filter(toNode -> modelDependencies.inDegreeOf(toNode) > 0)
                .forEach(toNode -> {
                    String toName = toNode.getName();
                    String fromNames = modelDependencies.incomingEdgesOf(toNode).stream().map(
                            edge -> edge.getSource().getName()
                    ).collect(Collectors.joining(", "));
                    logger.info(toName + " depends on " + fromNames);
                });
    }

    public static class MyEdge extends DefaultEdge {
        @Override
        protected ModelNode getSource() {
            return (ModelNode) super.getSource();
        }

        @Override
        protected ModelNode getTarget() {
            return (ModelNode) super.getTarget();
        }
    }
}
