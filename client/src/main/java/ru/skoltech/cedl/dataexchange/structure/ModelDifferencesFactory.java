package ru.skoltech.cedl.dataexchange.structure;

import ru.skoltech.cedl.dataexchange.structure.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.view.ChangeType;
import ru.skoltech.cedl.dataexchange.structure.view.ModelDifference;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public class ModelDifferencesFactory {

    public static List<ModelDifference> computeDifferences(ModelNode m1, ModelNode m2) {
        LinkedList<ModelDifference> modelDifferences = new LinkedList<>();
        if (!m1.getName().equals(m2.getName())) {
            String nodePath = m1.getNodePath() + "::name";
            String fromValue = m1.getName();
            String toValue = m2.getName();
            modelDifferences.add(new ModelDifference(nodePath, ChangeType.CHANGE_NODE_ATTRIBUTE, fromValue, toValue));
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
        Map<String, Object> m1SubNodesMap = (Map<String, Object>) m1.getSubNodes().stream().collect(
                Collectors.toMap(ModelNode::getUuid, Function.<ModelNode>identity())
        );
        Map<String, Object> m2SubNodesMap = (Map<String, Object>) m2.getSubNodes().stream().collect(
                Collectors.toMap(ModelNode::getUuid, Function.<ModelNode>identity())
        );

        Set<String> allSubnodes = new HashSet<>();
        allSubnodes.addAll(m1SubNodesMap.keySet());
        allSubnodes.addAll(m2SubNodesMap.keySet());

        for (String nodeUuid : allSubnodes) {
            ModelNode s1 = (ModelNode) m1SubNodesMap.get(nodeUuid);
            ModelNode s2 = (ModelNode) m2SubNodesMap.get(nodeUuid);

            if (s1 != null && s2 == null) {
                subnodesDifferences.add(new ModelDifference(s1.getNodePath(), ChangeType.REMOVE_NODE, s1.getName(), ""));
            } else if (s1 == null && s2 != null) {
                subnodesDifferences.add(new ModelDifference(s2.getNodePath(), ChangeType.ADD_NODE, "", s2.getName()));
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
                extModelDifferences.add(new ModelDifference(e1.getNodePath(), ChangeType.REMOVE_EXTERNALS_MODEL, e1.getName(), ""));
            } else if (e1 == null && e2 != null) {
                extModelDifferences.add(new ModelDifference(e2.getNodePath(), ChangeType.ADD_EXTERNAL_MODEL, "", e2.getName()));
            } else {
                if (!Arrays.equals(e1.getAttachment(), e2.getAttachment())) {
                    extModelDifferences.add(new ModelDifference(e1.getNodePath(), ChangeType.CHANGE_EXTERNAL_MODEL));
                }
            }
        }
        return extModelDifferences;
    }

    private static List<ModelDifference> differencesOnParameters(ModelNode m1, ModelNode m2) {
        LinkedList<ModelDifference> parameterDifferences = new LinkedList<>();
        Map<String, ParameterModel> m1params = m1.getParameters().stream().collect(
                Collectors.toMap(ParameterModel::getUuid, Function.identity())
        );
        Map<String, ParameterModel> m2params = m2.getParameters().stream().collect(
                Collectors.toMap(ParameterModel::getUuid, Function.identity())
        );
        Set<String> allParams = new HashSet<>();
        allParams.addAll(m1params.keySet());
        allParams.addAll(m2params.keySet());

        for (String parUuid : allParams) {
            ParameterModel p1 = m1params.get(parUuid);
            ParameterModel p2 = m2params.get(parUuid);

            if (p1 != null && p2 == null) {
                parameterDifferences.add(new ModelDifference(p1.getNodePath(), ChangeType.REMOVE_PARAMETER, p1.getName(), ""));
            } else if (p1 == null && p2 != null) {
                parameterDifferences.add(new ModelDifference(p2.getNodePath(), ChangeType.ADD_PARAMETER, "", p2.getName()));
            } else if (p1 != null && p2 != null) {
                List<AttributeDifference> differences = parameterDifferences(p1, p2);
                if (!differences.isEmpty()) {
                    ModelDifference modelDifference = new ModelDifference(p1.getNodePath(), ChangeType.MODIFY_PARAMETER);
                    if (p1.getLastModification() < p2.getLastModification()) {
                        modelDifference.setValue2("NEWER");
                    } else {
                        modelDifference.setValue1("NEWER");
                    }
                    parameterDifferences.add(modelDifference);
                }
                for (AttributeDifference diff : differences) {
                    String nodePath = p1.getNodePath() + "." + diff.attributeName;
                    ChangeType changeParameterAttribute = diff.attributeName.equals("value") || diff.attributeName.equals("overrideValue") ?
                            ChangeType.CHANGE_PARAMETER_VALUE : ChangeType.CHANGE_PARAMETER_ATTRIBUTE;
                    ModelDifference modelDifference = new ModelDifference(nodePath, changeParameterAttribute, diff.value1, diff.value2);

                    parameterDifferences.add(modelDifference);
                }
            }
        }
        return parameterDifferences;
    }

    private static List<AttributeDifference> parameterDifferences(ParameterModel p1, ParameterModel p2) {
        List<AttributeDifference> differences = new LinkedList<>();
        if ((p1.getUuid() == null && p2.getUuid() != null) || (p1.getUuid() != null && p2.getUuid() == null)
                || (p1.getUuid() != null && !p1.getUuid().equals(p2.getUuid()))) {
            differences.add(new AttributeDifference("uuid", p1.getUuid(), p2.getUuid()));
        }
        if ((p1.getName() == null && p2.getName() != null) || (p1.getName() != null && p2.getName() == null)
                || (p1.getName() != null && !p1.getName().equals(p2.getName()))) {
            differences.add(new AttributeDifference("name", p1.getName(), p2.getName()));
        }
        if ((p1.getValue() == null && p2.getValue() != null) || (p1.getValue() != null && p2.getValue() == null)
                || (p1.getValue() != null && !p1.getValue().equals(p2.getValue()))) {
            differences.add(new AttributeDifference("value", p1.getValue(), p2.getValue()));
        }
        if (!p1.getIsReferenceValueOverridden() == p2.getIsReferenceValueOverridden()) {
            differences.add(new AttributeDifference("isReferenceValueOverridden", p1.getIsReferenceValueOverridden(), p2.getIsReferenceValueOverridden()));
        }
        if ((p1.getOverrideValue() == null && p2.getOverrideValue() != null) || (p1.getOverrideValue() != null && p2.getOverrideValue() == null)
                || (p1.getOverrideValue() != null && !p1.getOverrideValue().equals(p2.getOverrideValue()))) {
            differences.add(new AttributeDifference("overrideValue", p1.getOverrideValue(), p2.getOverrideValue()));
        }
        if ((p1.getUnit() == null && p2.getUnit() != null) || (p1.getUnit() != null && p2.getUnit() == null)
                || (p1.getUnit() != null && !p1.getUnit().equals(p2.getUnit()))) {
            differences.add(new AttributeDifference("unit", p1.getUnit() != null ? p1.getUnit().asText() : null, p2.getUnit() != null ? p2.getUnit().asText() : null));
        }
        if ((p1.getNature() == null && p2.getNature() != null) || (p1.getNature() != null && p2.getNature() == null)
                || (p1.getNature() != null && !p1.getNature().equals(p2.getNature()))) {
            differences.add(new AttributeDifference("nature", p1.getNature(), p2.getNature()));
        }
        if ((p1.getValueSource() == null && p2.getValueSource() != null) || (p1.getValueSource() != null && p2.getValueSource() == null)
                || (p1.getValueSource() != null && !p1.getValueSource().equals(p2.getValueSource()))) {
            differences.add(new AttributeDifference("valueSource", p1.getValueSource(), p2.getValueSource()));
        }
        if ((p1.getValueReference() == null && p2.getValueReference() != null) || (p1.getValueReference() != null && p2.getValueReference() == null)
                || (p1.getValueReference() != null && !p1.getValueReference().equals(p2.getValueReference()))) {
            differences.add(new AttributeDifference("valueReference", p1.getValueReference(), p2.getValueReference()));
        }
        if ((p1.getValueLink() == null && p2.getValueLink() != null) || (p1.getValueLink() != null && p2.getValueLink() == null)
                || (p1.getValueLink() != null && !p1.getValueLink().equals(p2.getValueLink()))) {
            differences.add(new AttributeDifference("valueLink", p1.getValueLink() != null ? p1.getValueLink().getNodePath() : null,
                    p2.getValueLink() != null ? p2.getValueLink().getNodePath() : null));
        }
        if (!p1.getIsExported() == p2.getIsExported()) {
            differences.add(new AttributeDifference("isExported", p1.getIsExported(), p2.getIsExported()));
        }
        if ((p1.getExportReference() == null && p2.getExportReference() != null) || (p1.getExportReference() != null && p2.getExportReference() == null)
                || (p1.getExportReference() != null && !p1.getExportReference().equals(p2.getExportReference()))) {
            differences.add(new AttributeDifference("exportReference", p1.getExportReference(), p2.getExportReference()));
        }
        if ((p1.getDescription() == null && p2.getDescription() != null) || (p1.getDescription() != null && p2.getDescription() == null)
                || (p1.getDescription() != null && !p1.getDescription().equals(p2.getDescription()))) {
            differences.add(new AttributeDifference("description", p1.getDescription(), p2.getDescription()));
        }
        return differences;
    }

    static class AttributeDifference {
        public String attributeName;
        public String value1;
        public String value2;

        public AttributeDifference(String attributeName, Object value1, Object value2) {
            this.attributeName = attributeName;
            this.value1 = String.valueOf(value1);
            this.value2 = String.valueOf(value2);
        }
    }
}
