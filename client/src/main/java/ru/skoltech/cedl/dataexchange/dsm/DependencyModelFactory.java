package ru.skoltech.cedl.dataexchange.dsm;

import ru.skoltech.cedl.dataexchange.structure.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 03.12.2016.
 */
public class DependencyModelFactory {

    public static DependencyModel makeModel(SystemModel systemModel, ParameterLinkRegistry.DependencyGraph dependencyGraph) {
        DependencyModel dependencyModel = new DependencyModel();

        List<SubSystemModel> subNodes = systemModel.getSubNodes();
        List<ModelNode> modelNodeList = new ArrayList<>(subNodes.size() + 1);
        modelNodeList.add(systemModel);
        modelNodeList.addAll(subNodes);
        modelNodeList.forEach(modelNode -> dependencyModel.addElement(modelNode.getName()));

        for (ModelNode fromVertex : modelNodeList) {
            String fromVertexName = fromVertex.getName();
            for (ModelNode toVertex : modelNodeList) {
                if (dependencyGraph.getAllEdges(fromVertex, toVertex) != null &&
                        dependencyGraph.getAllEdges(fromVertex, toVertex).size() > 0) {
                    Set<String> linkedParams = getLinkedParams(fromVertex, toVertex);
                    int strength = linkedParams.size();
                    String parameterNames = linkedParams.stream().collect(Collectors.joining(",\n"));
                    String toVertexName = toVertex.getName();
                    dependencyModel.addConnection(fromVertexName, toVertexName, parameterNames, strength);
                }
            }
        }
        return dependencyModel;
    }

    private static Set<String> getLinkedParams(ModelNode toVertex, ModelNode fromVertex) {
        Set<String> sources = new TreeSet<>();
        ParameterTreeIterator it = new ParameterTreeIterator(fromVertex);
        while (it.hasNext()) {
            ParameterModel pm = it.next();
            if (pm.getValueSource() == ParameterValueSource.LINK &&
                    pm.getValueLink() != null && pm.getValueLink().getParent() != null &&
                    pm.getValueLink().getParent().getUuid().equals(toVertex.getUuid())) {
                sources.add(pm.getValueLink().getName());
            }
        }
        return sources;
    }

    public static NumericalDSM makeNumericalDSM(SystemModel systemModel, ParameterLinkRegistry.DependencyGraph dependencyGraph) {
        final List<SubSystemModel> subNodes = systemModel.getSubNodes();
        final List<ModelNode> modelNodeList = new ArrayList<>(subNodes.size() + 1);
        modelNodeList.add(systemModel);
        modelNodeList.addAll(subNodes);

        final int matrixSize = modelNodeList.size();
        NumericalDSM dsm = new NumericalDSM();

        for (int rowIndex = 0; rowIndex < matrixSize; rowIndex++) {
            ModelNode toVertex = modelNodeList.get(rowIndex);
            dsm.addElementName(toVertex.getName());
            for (int columnIndex = 0; columnIndex < matrixSize; columnIndex++) {
                ModelNode fromVertex = modelNodeList.get(columnIndex);
                if (dependencyGraph.getAllEdges(toVertex, fromVertex) != null &&
                        dependencyGraph.getAllEdges(toVertex, fromVertex).size() > 0) {
                    Set<String> linkedParams = getLinkedParams(toVertex, fromVertex);
                    int linkCount = linkedParams.size();
                    dsm.addLink(rowIndex + 1, columnIndex + 1, linkCount);
                }
            }
        }
        return dsm;
    }
}
