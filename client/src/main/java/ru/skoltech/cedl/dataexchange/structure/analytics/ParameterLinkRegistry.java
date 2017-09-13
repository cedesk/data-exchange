/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.structure.analytics;

import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.ParameterTreeIterator;
import ru.skoltech.cedl.dataexchange.entity.ParameterValueSource;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.logging.ActionLogger;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 08.09.2015.
 */
public class ParameterLinkRegistry {

    private Logger logger = Logger.getLogger(ParameterLinkRegistry.class);

    private Project project;
    private ActionLogger actionLogger;

    private Map<String, Set<String>> valueLinks = new HashMap<>();

    private DependencyGraph dependencyGraph = new DependencyGraph();

    public void setProject(Project project) {
        this.project = project;
    }

    public void setActionLogger(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    private static List<ModelNode> getModelNodes(SystemModel systemModel) {
        final List<SubSystemModel> subNodes = systemModel.getSubNodes();
        final List<ModelNode> modelNodeList = new ArrayList<>(subNodes.size() + 1);
        modelNodeList.add(systemModel);
        modelNodeList.addAll(subNodes);
        return modelNodeList;
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
        } else {
            logger.warn("trying to add self reference on node " + sourceModel.getNodePath());
        }
    }

    public void addLinks(List<ParameterModel> sources, ParameterModel sink) {
        for (ParameterModel source : sources) {
            addLink(source, sink);
        }
    }

    public void clear() {
        valueLinks.clear();
        dependencyGraph = new DependencyGraph();
    }

    public List<ParameterModel> getDependentParameters(ParameterModel source) {
        List<ParameterModel> dependentParameters = new LinkedList<>();
        SystemModel systemModel = source.getParent().findRoot();
        Map<String, ParameterModel> parameterDictionary = systemModel.makeParameterDictionary();

        String sourceId = source.getUuid();
        if (valueLinks.containsKey(sourceId)) {
            Set<String> sinks = valueLinks.get(sourceId);
            sinks.forEach(sinkUuid -> {
                ParameterModel sink = parameterDictionary.get(sinkUuid);
                dependentParameters.add(sink);
            });
        }
        return dependentParameters;
    }

    public String getDownstreamDependencies(ModelNode modelNode) {
        if (dependencyGraph.containsVertex(modelNode) && dependencyGraph.outDegreeOf(modelNode) > 0) {
            Set<ModelDependency> sinkDependencies = dependencyGraph.outgoingEdgesOf(modelNode);
            return sinkDependencies.stream().map(
                    dependency -> dependency.getTarget().getName()
            ).collect(Collectors.joining(", "));
        }
        return "";
    }

    public String getUpstreamDependencies(ModelNode modelNode) {
        if (dependencyGraph.containsVertex(modelNode) && dependencyGraph.inDegreeOf(modelNode) > 0) {
            Set<ModelDependency> sourceDependencies = dependencyGraph.incomingEdgesOf(modelNode);
            return sourceDependencies.stream().map(
                    dependency -> dependency.getSource().getName()
            ).collect(Collectors.joining(", "));
        }
        return "";
    }

    public DependencyModel makeDependencyModel(SystemModel rootNode) {
        DependencyModel dependencyModel = new DependencyModel();
        List<ModelNode> modelNodeList = getModelNodes(rootNode);
        modelNodeList.forEach(modelNode -> dependencyModel.addElement(modelNode.getName()));

        for (ModelNode fromVertex : modelNodeList) {
            String fromVertexName = fromVertex.getName();
            for (ModelNode toVertex : modelNodeList) {
                if (dependencyGraph.getAllEdges(fromVertex, toVertex) != null &&
                        dependencyGraph.getAllEdges(fromVertex, toVertex).size() > 0) {
                    Collection<ParameterModel> linkingParams = getLinkingParams(fromVertex, toVertex);
                    String toVertexName = toVertex.getName();
                    dependencyModel.addConnection(fromVertexName, toVertexName, linkingParams);
                }
            }
        }
        return dependencyModel;
    }

    public NumericalDSM makeNumericalDSM(SystemModel systemModel) {
        final List<ModelNode> modelNodeList = getModelNodes(systemModel);

        final int matrixSize = modelNodeList.size();
        NumericalDSM dsm = new NumericalDSM();

        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            ModelNode toVertex = modelNodeList.get(rowIndex);
            dsm.addElementName(toVertex.getName());
            for (int columnIndex = 0; columnIndex < matrixSize; columnIndex++) {
                ModelNode fromVertex = modelNodeList.get(columnIndex);
                if (dependencyGraph.getAllEdges(toVertex, fromVertex) != null &&
                        dependencyGraph.getAllEdges(toVertex, fromVertex).size() > 0) {
                    int linkCount = getLinkingParams(toVertex, fromVertex).size();
                    dsm.addLink(rowIndex + 1, columnIndex + 1, linkCount);
                }
            }
        }
        return dsm;
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

    public void removeLinks(List<ParameterModel> sources, ParameterModel sink) {
        for (ParameterModel source : sources) {
            removeLink(source, sink);
        }
    }

    public void removeSink(ParameterModel sink) {
        if (sink.getValueSource() == ParameterValueSource.LINK && sink.getValueLink() != null) {
            ParameterModel source = sink.getValueLink();
            removeLink(source, sink);
        }
    }

    public void updateAll(SystemModel systemModel) {
        logger.debug("updating all linked values");
        ParameterTreeIterator pmi = getLinkedParameters(systemModel);
        pmi.forEachRemaining(sink -> {
            ParameterModel source = sink.getValueLink();
            updateSinks(source); // TODO each call does a lot of common work
        });
        pmi = getCalculatedParameters(systemModel);
        pmi.forEachRemaining(this::recalculate);
    }

    public void updateSinks(ParameterModel source) {
        String sourceId = source.getUuid();
        if (valueLinks.containsKey(sourceId)) {
            SystemModel systemModel = source.getParent().findRoot();
            Map<String, ParameterModel> parameterDictionary = systemModel.makeParameterDictionary();

            Set<String> sinkIds = valueLinks.get(sourceId);
            for (String sinkId : sinkIds) {
                ParameterModel sink = parameterDictionary.get(sinkId);
                boolean editable = project.checkUserAccess(sink.getParent());
                if (!editable) continue;
                if (sink.getValueSource() == ParameterValueSource.LINK) {
                    if (sink.getValueLink() == source) {
                        // propagate only if actual change to value
                        Double sinkValue = sink.getValue();
                        double sourceEffectiveValue = source.getEffectiveValue();
                        if (!Precision.equals(sinkValue, sourceEffectiveValue, 2)) {
                            sink.setValue(sourceEffectiveValue);
                            logger.info("updated sink '" + sink.getNodePath() + "' from source '" + source.getNodePath()
                                    + "' [value: " + sinkValue + " -> " + sourceEffectiveValue + "]");
                            actionLogger.log(ActionLogger.ActionType.PARAMETER_MODIFY_LINK, sink.getNodePath()
                                    + " [value: " + sinkValue + " -> " + sourceEffectiveValue + "]");
                        }
                        // propagate only if actual change to unit
                        Unit sinkUnit = sink.getUnit();
                        Unit sourceUnit = source.getUnit();
                        if (sinkUnit == null || !sinkUnit.equals(sourceUnit)) {
                            sink.setUnit(sourceUnit);
                            String sinkUnitText = sinkUnit != null ? sinkUnit.asText() : null;
                            String sourceUnitText = sourceUnit != null ? sourceUnit.asText() : null;
                            logger.info("updated sink '" + sink.getNodePath() + "' from source '" + source.getNodePath()
                                    + "' [unit: " + sinkUnitText + " -> " + sourceUnitText + "]");
                            actionLogger.log(ActionLogger.ActionType.PARAMETER_MODIFY_LINK, sink.getNodePath()
                                    + " [unit: " + sinkUnitText + " -> " + sourceUnitText + "]");
                        }
                        // TODO: notify UI ?]
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

    private ParameterTreeIterator getCalculatedParameters(SystemModel systemModel) {
        Objects.requireNonNull(systemModel);
        return new ParameterTreeIterator(systemModel,
                sink -> sink.getValueSource() == ParameterValueSource.CALCULATION &&
                        sink.getCalculation() != null);
    }

    private ParameterTreeIterator getLinkedParameters(SystemModel systemModel) {
        Objects.requireNonNull(systemModel);
        return new ParameterTreeIterator(systemModel,
                sink -> sink.getNature() == ParameterNature.INPUT &&
                        sink.getValueSource() == ParameterValueSource.LINK &&
                        sink.getValueLink() != null);
    }

    private Collection<ParameterModel> getLinkingParams(ModelNode toVertex, ModelNode fromVertex) {
        List<ParameterModel> sources = new LinkedList<>();
        Iterator<ParameterModel> it;
        if (fromVertex.getParent() == null) { // if root, only consider its immediate parameters
            it = fromVertex.getParameters().iterator();
        } else { // if other node, consider all parameters of subnodes
            it = new ParameterTreeIterator(fromVertex);
        }
        while (it.hasNext()) {
            ParameterModel pm = it.next();
            if (pm.getValueSource() == ParameterValueSource.LINK &&
                    pm.getValueLink() != null && pm.getValueLink().getParent() != null &&
                    pm.getValueLink().getParent().getUuid().equals(toVertex.getUuid())) {
                sources.add(pm);
            }
        }
        logger.debug("from: " + fromVertex.getName() + ", to: " + toVertex.getName());
        sources.forEach(parameterModel -> logger.debug("\t" + parameterModel.getNodePath() + " -> " + parameterModel.getValueLink().getNodePath()));
        return sources;
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

    private void recalculate(ParameterModel sink) {
        if (sink.getCalculation() != null) {
            if (sink.getCalculation().valid()) {
                logger.info("updating sink '" + sink.getNodePath() + "' from calculation");
                sink.setValue(sink.getCalculation().evaluate());
            } else {
                logger.info("invalid calculation '" + sink.getNodePath() + "'");
            }
        } else {
            logger.info("empty calculation '" + sink.getNodePath() + "'");
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

    public static class DependencyGraph extends SimpleDirectedGraph<ModelNode, ModelDependency> {
        DependencyGraph() {
            super(ModelDependency.class);
        }
    }
}
