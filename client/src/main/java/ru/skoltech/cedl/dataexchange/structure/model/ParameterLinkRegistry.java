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
    private DirectedGraph<ModelNode, ModelDependency> dependencyGraph = new SimpleDirectedGraph<>(ModelDependency.class);

    public ParameterLinkRegistry() {
    }

    public void registerAllParameters(SystemModel systemModel) {
        clear();
        ParameterTreeIterator pmi = getLinkedParameters(systemModel);
        pmi.forEachRemaining(sink -> {
            ParameterModel source = sink.getValueLink();
            addLink(source, sink);
        });
        printDependencies(dependencyGraph);
    }

    public ParameterTreeIterator getLinkedParameters(SystemModel systemModel) {
        Objects.requireNonNull(systemModel);
        return new ParameterTreeIterator(systemModel,
                sink -> sink.getNature() == ParameterNature.INPUT &&
                        sink.getValueSource() == ParameterValueSource.LINK &&
                        sink.getValueLink() != null);
    }

    public void addLink(ParameterModel source, ParameterModel sink) {
        System.out.println("sink '" + sink.getNodePath() + "' is linking to source '" + source.getNodePath() + "'");
        String sourceId = source.getUuid();
        if (valueLinks.containsKey(sourceId)) {
            valueLinks.get(sourceId).add(sink);
        } else {
            Set<ParameterModel> sinks = new TreeSet<>();
            sinks.add(sink);
            valueLinks.put(sourceId, sinks);
        }

        ModelNode sourceModel = source.getParent();
        ModelNode sinkModel = sink.getParent();
        dependencyGraph.addVertex(sinkModel);
        dependencyGraph.addVertex(sourceModel);
        // dependency goes from SOURCE to SINK
        dependencyGraph.addEdge(sourceModel, sinkModel);
    }

    public void updateAll(SystemModel systemModel) {
        logger.debug("updating all linked values");
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
                    parameterModel.setValue(source.getEffectiveValue());
                    parameterModel.setUnit(source.getUnit());
                    // TODO: notify UI ?
                } else {
                    logger.error("model changed, sink '" + parameterModel.getNodePath() + "' no longer referencing '" + source.getNodePath() + "'");
                }
            }
        } else {
            logger.debug("parameter '" + source.getNodePath() + "' is never linked");
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
        // dependency goes from SOURCE to SINK
        ModelNode sourceModel = source.getParent();
        ModelNode sinkModel = sink.getParent();
        dependencyGraph.removeEdge(sourceModel, sinkModel);
    }

    public void clear() {
        valueLinks.clear();
        dependencyGraph = new SimpleDirectedGraph<>(ModelDependency.class);
    }

    public String getUpstreamDependencies(ModelNode modelNode) {
        if (dependencyGraph.containsVertex(modelNode) && dependencyGraph.inDegreeOf(modelNode) > 0) {
            Set<ModelDependency> sourceDependencies = dependencyGraph.incomingEdgesOf(modelNode);
            String sourceNames = sourceDependencies.stream().map(
                    dependency -> dependency.getSource().getName()
            ).collect(Collectors.joining(", "));
            return sourceNames;
        }
        return "";
    }

    public String getDownstreamDependencies(ModelNode modelNode) {
        if (dependencyGraph.containsVertex(modelNode) && dependencyGraph.outDegreeOf(modelNode) > 0) {
            Set<ModelDependency> sinkDependencies = dependencyGraph.outgoingEdgesOf(modelNode);
            String sinkNames = sinkDependencies.stream().map(
                    dependency -> dependency.getTarget().getName()
            ).collect(Collectors.joining(", "));
            return sinkNames;
        }
        return "";
    }

    private void printDependencies(DirectedGraph<ModelNode, ModelDependency> modelDependencies) {
        System.out.println("DEPENDENCIES");
        modelDependencies.vertexSet().stream()
                .filter(sourceNode -> modelDependencies.inDegreeOf(sourceNode) > 0)
                .forEach(sinkNode -> {
                    String sinkName = sinkNode.getName();
                    String sourceNames = modelDependencies.incomingEdgesOf(sinkNode).stream().map(
                            dependency -> dependency.getSource().getName()
                    ).collect(Collectors.joining(", "));
                    System.out.println(sinkName + " depends on " + sourceNames);
                });
    }

    public void removeLinks(ParameterModel sink, List<ParameterModel> sources) {
        for (ParameterModel source : sources) {
            removeLink(source, sink);
        }
    }

    public void addLinks(ParameterModel sink, List<ParameterModel> sources) {
        for (ParameterModel source : sources) {
            addLink(source, sink);
        }
    }

    public static class ModelDependency extends DefaultEdge {
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
