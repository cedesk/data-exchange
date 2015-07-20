package ru.skoltech.cedl.dataexchange.structure;

import ru.skoltech.cedl.dataexchange.structure.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.view.ChangeType;
import ru.skoltech.cedl.dataexchange.structure.view.ModelDifference;

import java.util.*;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public class ModelDifferencesFactory {

    public static List<ModelDifference> computeDifferences(ModelNode m1, ModelNode m2) {
        LinkedList<ModelDifference> modelDifferences = new LinkedList<>();
        if (!m1.getName().equals(m2.getName())) {
            String nodePath = m1.getNodePath() + "::name";
            String changeValue = m2.getName();
            modelDifferences.add(new ModelDifference(nodePath, ChangeType.CHANGE_NODE_ATTRIBUTE, changeValue));
        }
        modelDifferences.addAll(differencesOnParameters(m1, m2));
        modelDifferences.addAll(differencesOnExternalModels(m1, m2));
        if (m1 instanceof CompositeModelNode && m2 instanceof CompositeModelNode) {
            modelDifferences.addAll(differencesOnSubNodes((CompositeModelNode) m1, (CompositeModelNode) m2));
        }
        return modelDifferences;
    }

    private static List<ModelDifference> differencesOnSubNodes(CompositeModelNode m1, CompositeModelNode m2) {
        LinkedList<ModelDifference> subnodesDifferences = new LinkedList<>();
        Map<String, ModelNode> m1SubNodesMap = m1.getSubNodesMap();
        Map<String, ModelNode> m2SubNodesMap = m2.getSubNodesMap();

        Set<String> allSubnodes = new HashSet<>();
        allSubnodes.addAll(m1SubNodesMap.keySet());
        allSubnodes.addAll(m2SubNodesMap.keySet());

        for (String subNod : allSubnodes) {
            ModelNode s1 = m1SubNodesMap.get(subNod);
            ModelNode s2 = m2SubNodesMap.get(subNod);

            if (s1 != null && s2 == null) {
                subnodesDifferences.add(new ModelDifference(s1.getNodePath(), ChangeType.REMOVE_NODE, ""));
            } else if (s1 == null && s2 != null) {
                subnodesDifferences.add(new ModelDifference(s2.getNodePath(), ChangeType.ADD_NODE, ""));
            } else {
                // depth search
                subnodesDifferences.addAll(computeDifferences(s1, s2));
            }
        }
        return subnodesDifferences;
    }

    private static List<ModelDifference> differencesOnExternalModels(ModelNode m1, ModelNode m2) {
        LinkedList<ModelDifference> extModelDifferences = new LinkedList<>();
        Map<String, ExternalModel> m1extModels = m1.getExternalModelMap();
        Map<String, ExternalModel> m2extModels = m2.getExternalModelMap();

        Set<String> allExtMods = new HashSet<>();
        allExtMods.addAll(m1extModels.keySet());
        allExtMods.addAll(m2extModels.keySet());

        for (String extMod : allExtMods) {
            ExternalModel e1 = m1extModels.get(extMod);
            ExternalModel e2 = m2extModels.get(extMod);

            if (e1 != null && e2 == null) {
                extModelDifferences.add(new ModelDifference(e1.getNodePath(), ChangeType.REMOVE_EXTERNALS_MODEL, ""));
            } else if (e1 == null && e2 != null) {
                extModelDifferences.add(new ModelDifference(e2.getNodePath(), ChangeType.ADD_EXTERNAL_MODEL, ""));
            } else {
                if (!Arrays.equals(e1.getAttachment(), e2.getAttachment())) {
                    extModelDifferences.add(new ModelDifference(e1.getNodePath(), ChangeType.CHANGE_EXTERNAL_MODEL, "attachment"));
                }
            }
        }
        return extModelDifferences;
    }

    private static List<ModelDifference> differencesOnParameters(ModelNode m1, ModelNode m2) {
        LinkedList<ModelDifference> parameterDifferences = new LinkedList<>();
        Map<String, ParameterModel> m1params = m1.getParameterMap();
        Map<String, ParameterModel> m2params = m2.getParameterMap();

        Set<String> allParams = new HashSet<>();
        allParams.addAll(m1params.keySet());
        allParams.addAll(m2params.keySet());

        for (String parName : allParams) {
            ParameterModel p1 = m1params.get(parName);
            ParameterModel p2 = m2params.get(parName);

            if (p1 != null && p2 == null) {
                parameterDifferences.add(new ModelDifference(p1.getNodePath(), ChangeType.REMOVE_PARAMETER, ""));
            } else if (p1 == null && p2 != null) {
                parameterDifferences.add(new ModelDifference(p2.getNodePath(), ChangeType.ADD_PARAMETER, ""));
            } else {
                Map<String, String> differences = p1.diff(p2);
                for (Map.Entry<String, String> diff : differences.entrySet()) {
                    String nodePath = p1.getNodePath() + "." + diff.getKey();
                    String changeValue = diff.getValue();
                    ChangeType changeParameterAttribute = diff.getKey().equals("value") || diff.getKey().equals("overrideValue") ?
                            ChangeType.CHANGE_PARAMETER_VALUE : ChangeType.CHANGE_PARAMETER_ATTRIBUTE;
                    parameterDifferences.add(new ModelDifference(nodePath, changeParameterAttribute, changeValue));
                }
            }
        }
        return parameterDifferences;
    }
}
