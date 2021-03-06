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

import edu.carleton.tim.jdsm.RealNumberDSM;
import edu.carleton.tim.jdsm.dependency.Dependency;
import edu.carleton.tim.jdsm.dependency.DependencyDSM;
import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jscience.mathematics.number.Real;
import ru.skoltech.cedl.dataexchange.entity.*;
import ru.skoltech.cedl.dataexchange.entity.calculation.Argument;
import ru.skoltech.cedl.dataexchange.entity.calculation.Calculation;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.unit.Unit;
import ru.skoltech.cedl.dataexchange.repository.jpa.CalculationRepository;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 08.09.2015.
 */
public class ParameterLinkRegistry {

    private Logger logger = Logger.getLogger(ParameterLinkRegistry.class);

    private Project project;
    private CalculationRepository calculationRepository;

    private Map<String, Set<String>> valueLinks = new HashMap<>();

    private DependencyGraph dependencyGraph = new DependencyGraph();

    public void setProject(Project project) {
        this.project = project;
    }

    public void setCalculationRepository(CalculationRepository calculationRepository) {
        this.calculationRepository = calculationRepository;
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
        if (sourceModel.getUuid().equals(sinkModel.getUuid())) { // do not record self-references to keep the graph acyclic
            // TODO: FIX: not recording this blocks evaluation of calculations within a node
            logger.warn("skipping same-node reference: " + sink.getNodePath() + " -> " + source.getNodePath());
            return;
        }
        dependencyGraph.addVertex(sinkModel);
        dependencyGraph.addVertex(sourceModel);
        // dependency goes from SOURCE to SINK
        dependencyGraph.addEdge(sourceModel, sinkModel);
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

    public DependencyModel makeDependencyModel(SystemModel systemModel, Comparator<ModelNode> comparator) {
        final List<ModelNode> modelNodeList = systemModel.getRootAndSubsystems(comparator);
        DependencyModel dependencyModel = new DependencyModel();

        modelNodeList.forEach(modelNode -> dependencyModel.addElement(modelNode.getName()));
        modelNodeList.stream()
                .map(m -> dependencyGraph.vertexSet().stream().filter(v -> v.getUuid().equals(m.getUuid())).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .forEach(fromVertex -> {
                    Set<ModelDependency> toVertexes = dependencyGraph.edgesOf(fromVertex);
                    toVertexes.forEach(md -> {
                        Collection<ParameterModel> linkingParams = getLinkingParams(fromVertex, md.getTarget());
                        String toVertexName = md.getTarget().getName();
                        dependencyModel.addConnection(fromVertex.getName(), toVertexName, linkingParams);
                    });
                });
        return dependencyModel;
    }

    public DependencyDSM makeBinaryDSM(SystemModel systemModel, Comparator<ModelNode> comparator) {
        final List<ModelNode> modelNodeList = systemModel.getRootAndSubsystems(comparator);
        final int matrixSize = modelNodeList.size();
        Map<String, Integer> namePositionMappings = new TreeMap<>();
        Map<Integer, String> positionNameMappings = new TreeMap<>();
        Dependency[][] map = new Dependency[matrixSize][matrixSize];
        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            ModelNode toVertex = modelNodeList.get(rowIndex);
            namePositionMappings.put(toVertex.getName(), rowIndex);
            positionNameMappings.put(rowIndex, toVertex.getName());
            // DSM matrix is in form IR/FAD
            // column index means the dependency source
            for (int columnIndex = 0; columnIndex < matrixSize; columnIndex++) {
                ModelNode fromVertex = modelNodeList.get(columnIndex);
                Set<ModelDependency> edges = dependencyGraph.getAllEdges(fromVertex, toVertex);
                if (edges != null && edges.size() > 0) {
                    map[rowIndex][columnIndex] = Dependency.YES;
                } else {
                    map[rowIndex][columnIndex] = Dependency.NO;
                }
            }
        }
        return new DependencyDSM(new HashMap<>(), new HashMap<>(),
                namePositionMappings, positionNameMappings, map);
    }

    public RealNumberDSM makeRealDSM(SystemModel systemModel, Comparator<ModelNode> comparator) {
        final List<ModelNode> modelNodeList = systemModel.getRootAndSubsystems(comparator);
        final int matrixSize = modelNodeList.size();
        Map<String, Integer> namePositionMappings = new HashMap<>();
        Map<Integer, String> positionNameMappings = new HashMap<>();
        Real[][] map = new Real[matrixSize][matrixSize];
        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            ModelNode toVertex = modelNodeList.get(rowIndex);
            namePositionMappings.put(toVertex.getName(), rowIndex);
            positionNameMappings.put(rowIndex, toVertex.getName());
            // DSM matrix is in form IR/FAD
            // column index means the dependency source
            for (int columnIndex = 0; columnIndex < matrixSize; columnIndex++) {
                ModelNode fromVertex = modelNodeList.get(columnIndex);
                Set<ModelDependency> edges = dependencyGraph.getAllEdges(fromVertex, toVertex);
                if (edges != null && edges.size() > 0) {
                    int linkCount = getLinkingParams(fromVertex, toVertex).size();
                    map[rowIndex][columnIndex] = Real.valueOf(linkCount);
                } else {
                    map[rowIndex][columnIndex] = Real.ZERO;
                }
            }
        }
        return new RealNumberDSM(new HashMap<>(), new HashMap<>(),
                namePositionMappings, positionNameMappings, map);
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
            Calculation calculation = calculationRepository.findOne(sink.getCalculation().getId());
            if (calculation == null) {
                return;
            }
            addLinks(calculation.getLinkedParameters(), sink);
        });
    }

    public void updateAllSinks(SystemModel systemModel, Predicate<ModelNode> accessChecker) {
        Iterator<ExternalModel> iterator = new ExternalModelTreeIterator(systemModel, accessChecker);
        iterator.forEachRemaining(externalModel -> externalModel.updateReferencedParameterModels(this::updateSinks));
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

    public void replaceLink(ParameterModel oldSource, ParameterModel newSource, ParameterModel sink) {
        if (oldSource != null) {
            this.removeLink(oldSource, sink);
        }
        if (newSource != null) {
            this.addLink(newSource, sink);
        }
    }

    public void replaceLinks(List<ParameterModel> oldSources, List<ParameterModel> newSources, ParameterModel sink) {
        if (oldSources != null) {
            this.removeLinks(oldSources, sink);
        }
        if (newSources != null) {
            this.addLinks(newSources, sink);
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
        logger.debug("from: " + fromVertex.getName() + ", to: " + toVertex.getName());
        while (it.hasNext()) {
            ParameterModel pm = it.next();
            if (pm.getValueSource() == ParameterValueSource.LINK &&
                    pm.getValueLink() != null && pm.getValueLink().getParent() != null &&
                    pm.getValueLink().getParent().getUuid().equals(toVertex.getUuid())) {
                logger.debug("\t" + pm.getNodePath() + " -> " + pm.getValueLink().getNodePath());
                sources.add(pm);
            }
            if (pm.getValueSource() == ParameterValueSource.CALCULATION && pm.getCalculation() != null) {
                Calculation calculation = calculationRepository.findOne(pm.getCalculation().getId());
                if (calculation != null) {
                    for (Argument argument : pm.getCalculation().getArguments()) {
                        if (argument instanceof Argument.Parameter) {
                            ParameterModel sourcePM = ((Argument.Parameter) argument).getLink();
                            if (sourcePM != null && sourcePM.getParent() != null &&
                                    sourcePM.getParent().getUuid().equals(toVertex.getUuid())) {
                                logger.debug("\t" + pm.getNodePath() + " -> " + sourcePM.getNodePath());
                                sources.add(pm);
                            }
                        }
                    }
                }
            }
        }
        return sources;
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
