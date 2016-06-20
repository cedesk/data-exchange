package ru.skoltech.cedl.dataexchange.structure.view;

import ru.skoltech.cedl.dataexchange.structure.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;

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
            String value1 = m1.getName();
            String value2 = m2.getName();
            modelDifferences.add(NodeDifference.createNodeAttributesModified(m1, m2, "name", value1, value2));
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
                if (s1.getLastModification() == null) {
                    subnodesDifferences.add(NodeDifference.createAddedNode(s1, s1.getName(), ChangeLocation.ARG1));
                } else {
                    subnodesDifferences.add(NodeDifference.createRemovedNode(s1, s1.getName(), ChangeLocation.ARG2));
                }
            } else if (s1 == null && s2 != null) {
                // TODO: distinguish between local remove and remote add
                subnodesDifferences.add(NodeDifference.createRemovedNode(s2, s2.getName(), ChangeLocation.ARG1));
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
                if (e1.getLastModification() == null) {
                    extModelDifferences.add(NodeDifference.createAddExternalModel(e1.getParent(), e1.getName(), ChangeLocation.ARG1));
                } else {
                    extModelDifferences.add(NodeDifference.createRemoveExternalModel(e1.getParent(), e1.getName(), ChangeLocation.ARG1));
                }
            } else if (e1 == null && e2 != null) {
                // TODO: distinguish between local remove and remote add
                extModelDifferences.add(NodeDifference.createRemoveExternalModel(e2.getParent(), e2.getName(), ChangeLocation.ARG1));
            } else {
                if (!Arrays.equals(e1.getAttachment(), e2.getAttachment())) {
                    boolean e2newer = e2.getLastModification() > e1.getLastModification();
                    ChangeLocation changeLocation = e2newer ? ChangeLocation.ARG2 : ChangeLocation.ARG1;
                    extModelDifferences.add(NodeDifference.createExternalModelModified(e1.getParent(), e2.getParent(), e1.getName(), changeLocation));
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
                if (p1.getLastModification() == null) { // parameter 1 was newly added
                    parameterDifferences.add(ParameterDifference.createAddedParameter(m1, p1, p1.getName(), ChangeLocation.ARG1));
                } else { // parameter 2 was deleted
                    parameterDifferences.add(ParameterDifference.createRemovedParameter(m1, p1, p1.getName(), ChangeLocation.ARG2));
                }
            } else if (p1 == null && p2 != null) {
                // TODO: distinguish between local remove and remote add
                parameterDifferences.add(ParameterDifference.createRemovedParameter(m1, p2, p2.getName(), ChangeLocation.ARG1));
            } else if (p1 != null && p2 != null) {
                List<AttributeDifference> differences = parameterDifferences(p1, p2);
                if (!differences.isEmpty()) {
                    ModelDifference modelDifference = ParameterDifference.createParameterAttributesModified(p1, p2, differences);
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
        ParameterModel vl1 = p1.getValueLink();
        ParameterModel vl2 = p2.getValueLink();
        if ((vl1 == null && vl2 != null) || (vl1 != null && vl2 == null)) {
            // TODO: fix NullPointerException
            if ((vl1 != null && !vl1.getUuid().equals(vl2.getUuid()))) { // reference is the same
                if ((vl1.getValue() == null && vl2.getValue() != null) || (vl1.getValue() != null && vl2.getValue() == null)
                        || (vl1.getValue() != null && !vl1.getValue().equals(vl2.getValue()))) {
                    differences.add(new AttributeDifference("valueLink>value", vl1.getValue(), vl2.getValue()));
                }
                if ((vl1.getUnit() == null && vl2.getUnit() != null) || (vl1.getUnit() != null && vl2.getUnit() == null)
                        || (vl1.getUnit() != null && !vl1.getUnit().equals(vl2.getUnit()))) {
                    differences.add(new AttributeDifference("valueLink>unit", vl1.getUnit() != null ? vl1.getUnit().asText() : null, vl2.getUnit() != null ? vl2.getUnit().asText() : null));
                }
            } else { // different reference
                differences.add(new AttributeDifference("valueLink", vl1 != null ? vl1.getNodePath() : null,
                        vl2 != null ? vl2.getNodePath() : null));
            }
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
}
