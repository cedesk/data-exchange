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

    private Map<String, Set<String>> valueLinks = new HashMap<>();
    private DirectedGraph<ModelNode, ModelDependency> dependencyGraph = new SimpleDirectedGraph<>(ModelDependency.class);

    public ParameterLinkRegistry() {
    }

    public DirectedGraph<ModelNode, ModelDependency> getDependencyGraph() {
        return dependencyGraph;
    }

    public void registerAllParameters(SystemModel systemModel) {
        clear();
        ParameterTreeIterator pmi = getLinkedParameters(systemModel);
        pmi.forEachRemaining(sink -> {
            ParameterModel source = sink.getValueLink();
            addLink(source, sink);
        });
        pmi = getCalculatedParameters(systemModel);
        pmi.forEachRemaining(sink -> {
            Calculation calculation = sink.getCalculation();
            addLinks(calculation.getLinkedParameters(), sink);
        });
        //printDependencies(dependencyGraph);
    }

    public ParameterTreeIterator getLinkedParameters(SystemModel systemModel) {
        Objects.requireNonNull(systemModel);
        return new ParameterTreeIterator(systemModel,
                sink -> sink.getNature() == ParameterNature.INPUT &&
                        sink.getValueSource() == ParameterValueSource.LINK &&
                        sink.getValueLink() != null);
    }

    public ParameterTreeIterator getCalculatedParameters(SystemModel systemModel) {
        Objects.requireNonNull(systemModel);
        return new ParameterTreeIterator(systemModel,
                sink -> sink.getValueSource() == ParameterValueSource.CALCULATION &&
                        sink.getCalculation() != null);
    }

    public void addLink(ParameterModel source, ParameterModel sink) {
        logger.debug("sink '" + sink.getNodePath() + "' is linking to source '" + source.getNodePath() + "'");
        String sourceId = source.getUuid();
        if (valueLinks.containsKey(sourceId)) {
            valueLinks.get(sourceId).add(sink.getUuid());
        } else {
            Set<String> sinks = new TreeSet<>();
            sinks.add(sink.getUuid());
            valueLinks.put(sourceId, sinks);
        }

        ModelNode sourceModel = source.getParent();
        ModelNode sinkModel = sink.getParent();
        if (sourceModel != sinkModel) { // do not record self-references to keep the graph acyclic
            dependencyGraph.addVertex(sinkModel);
            dependencyGraph.addVertex(sourceModel);
            // dependency goes from SOURCE to SINK
            dependencyGraph.addEdge(sourceModel, sinkModel);
        }
    }

    public void updateAll(SystemModel systemModel) {
        logger.debug("updating all linked values");
        ParameterTreeIterator pmi = getLinkedParameters(systemModel);
        pmi.forEachRemaining(sink -> {
            ParameterModel source = sink.getValueLink();
            updateSinks(source);
        });
        pmi = getCalculatedParameters(systemModel);
        pmi.forEachRemaining(sink -> {
            recalculate(sink);
        });
    }

    private void recalculate(ParameterModel sink) {
        logger.info("updating sink '" + sink.getNodePath() + "' from calculation");
        sink.setValue(sink.getCalculation().evaluate());
    }

    public void updateSinks(ParameterModel source) {
        SystemModel systemModel = getSystem(source.getParent());
        Map<String, ParameterModel> parameterDictionary = makeDictionary(systemModel);

        String sourceId = source.getUuid();
        if (valueLinks.containsKey(sourceId)) {
            Set<String> sinkIds = valueLinks.get(sourceId);
            for (String sinkId : sinkIds) {
                ParameterModel sink = parameterDictionary.get(sinkId);
                if (sink.getValueSource() == ParameterValueSource.LINK) {
                    if (sink.getValueLink() == source) {
                        logger.info("updating sink '" + sink.getNodePath() + "' from source '" + source.getNodePath() + "'");
                        sink.setValue(source.getEffectiveValue());
                        sink.setUnit(source.getUnit());
                        // TODO: notify UI ?
                    } else {
                        logger.error("model changed, sink '" + sink.getNodePath() + "' no longer referencing '" + source.getNodePath() + "'");
                    }
                } else if (sink.getValueSource() == ParameterValueSource.CALCULATION) {
                    recalculate(sink);
                }
            }
        } else {
            logger.debug("parameter '" + source.getNodePath() + "' is never linked");
        }
    }

    private Map<String, ParameterModel> makeDictionary(SystemModel systemModel) {
        Map<String, ParameterModel> dictionary = new HashMap<>();
        Iterator<ParameterModel> pmi = systemModel.parametersTreeIterator();
        pmi.forEachRemaining(parameterModel -> dictionary.put(parameterModel.getUuid(), parameterModel));
        return dictionary;
    }

    public void removeLink(ParameterModel source, ParameterModel sink) {
        String sourceId = source.getUuid();
        if (valueLinks.containsKey(sourceId)) {
            Set<String> sinks = valueLinks.get(sourceId);
            sinks.remove(sink.getUuid());
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

    private SystemModel getSystem(ModelNode parent) {
        SystemModel result = null;
        if (parent != null) {
            while (parent.getParent() != null) {
                parent = parent.getParent();
            }
            result = (SystemModel) parent;
        }
        return result;
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

    public void removeLinks(List<ParameterModel> sources, ParameterModel sink) {
        for (ParameterModel source : sources) {
            removeLink(source, sink);
        }
    }

    public void addLinks(List<ParameterModel> sources, ParameterModel sink) {
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
