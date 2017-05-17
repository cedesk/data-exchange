package ru.skoltech.cedl.dataexchange.structure.model.diff;

import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 20.07.2015.
 */
public class ModelDifferencesFactory {

    public static List<ModelDifference> computeDifferences(Study s1, Study s2, long latestStudy1Modification) {
        List<ModelDifference> modelDifferences = new LinkedList<>();

        Long lmm1 = s1.getLatestModelModification();
        Long lmm2 = s2.getLatestModelModification();
        if (!Objects.equals(lmm1, lmm2)) {
            modelDifferences.add(StudyDifference.createStudyAttributesModified(s1, s2, "latestModelModification", lmm1.toString(), lmm2.toString()));
        }

        long s1Version = s1.getVersion();
        long s2Version = s2.getVersion();
        if (s1Version != s2Version) {
            modelDifferences.add(StudyDifference.createStudyAttributesModified(s1, s2, "version", Long.toString(s1Version), Long.toString(s2Version)));
        }

        UserRoleManagement urm1 = s1.getUserRoleManagement();
        UserRoleManagement urm2 = s2.getUserRoleManagement();
        if (!urm1.equals(urm2)) {
            modelDifferences.add(StudyDifference.createStudyAttributesModified(s1, s2, "userRoleManagement", "<>", "<>"));
        }

        StudySettings ss1 = s1.getStudySettings();
        StudySettings ss2 = s1.getStudySettings();
        if (!ss1.equals(ss2)) {
            modelDifferences.add(StudyDifference.createStudyAttributesModified(s1, s2, "studySettings", "<>", "<>"));
        }

        modelDifferences.addAll(computeDifferences(s1.getSystemModel(), s2.getSystemModel(), latestStudy1Modification));
        return modelDifferences;
    }

    public static List<ModelDifference> computeDifferences(ModelNode m1, ModelNode m2, long latestStudy1Modification) {
        LinkedList<ModelDifference> modelDifferences = new LinkedList<>();
        if (!m1.getName().equals(m2.getName())) {
            String value1 = m1.getName();
            String value2 = m2.getName();
            modelDifferences.add(NodeDifference.createNodeAttributesModified(m1, m2, "name", value1, value2));
        }
        modelDifferences.addAll(differencesOnParameters(m1, m2, latestStudy1Modification));
        modelDifferences.addAll(differencesOnExternalModels(m1, m2, latestStudy1Modification));
        if (m1 instanceof CompositeModelNode && m2 instanceof CompositeModelNode) {
            modelDifferences.addAll(differencesOnSubNodes((CompositeModelNode) m1, (CompositeModelNode) m2, latestStudy1Modification));
        }
        return modelDifferences;
    }

    private static List<ModelDifference> differencesOnSubNodes(CompositeModelNode m1, CompositeModelNode m2, long latestStudy1Modification) {
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
                if (s2.getLastModification() <= latestStudy1Modification) { // node 2 was deleted
                    subnodesDifferences.add(NodeDifference.createRemovedNode(s2, s2.getName(), ChangeLocation.ARG1));
                } else { // node 2 was added
                    subnodesDifferences.add(NodeDifference.createAddedNode(s2, s2.getName(), ChangeLocation.ARG2));
                }
            } else {
                // depth search
                subnodesDifferences.addAll(computeDifferences(s1, s2, latestStudy1Modification));
            }
        }
        return subnodesDifferences;
    }

    private static List<ModelDifference> differencesOnExternalModels(ModelNode m1, ModelNode m2, Long latestStudy1Modification) {
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
                if (e2.getLastModification() <= latestStudy1Modification) { // model 2 was deleted
                    extModelDifferences.add(NodeDifference.createRemoveExternalModel(e2.getParent(), e2.getName(), ChangeLocation.ARG1));
                } else { // model 1 was added
                    extModelDifferences.add(NodeDifference.createAddExternalModel(e2.getParent(), e2.getName(), ChangeLocation.ARG2));
                }
            } else if (e1 != null && e2 != null) {
                if (!Arrays.equals(e1.getAttachment(), e2.getAttachment())) {
                    boolean e2newer = e2.getLastModification() > e1.getLastModification();
                    ChangeLocation changeLocation = e2newer ? ChangeLocation.ARG2 : ChangeLocation.ARG1;
                    extModelDifferences.add(NodeDifference.createExternalModelModified(e1.getParent(), e2.getParent(), e1.getName(), changeLocation));
                }
            }
        }
        return extModelDifferences;
    }

    private static List<ModelDifference> differencesOnParameters(ModelNode m1, ModelNode m2, long latestStudy1Modification) {
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
                if (p2.getLastModification() <= latestStudy1Modification) { // parameter 2 was deleted
                    parameterDifferences.add(ParameterDifference.createRemovedParameter(m1, p2, p2.getName(), ChangeLocation.ARG1));
                } else { // parameter 1 was added
                    parameterDifferences.add(ParameterDifference.createAddedParameter(m1, p2, p2.getName(), ChangeLocation.ARG2));
                }
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

    public static List<AttributeDifference> parameterDifferences(ParameterModel p1, ParameterModel p2) {
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
        if (vl1 != null && vl2 != null) {
            if (!vl1.getUuid().equals(vl2.getUuid())) { // reference is the same
                differences.add(new AttributeDifference("valueLink", vl1.getNodePath(), vl2.getNodePath()));
            } else { // different reference
                if ((vl1.getValue() == null && vl2.getValue() != null) || (vl1.getValue() != null && vl2.getValue() == null)
                        || (vl1.getValue() != null && !vl1.getValue().equals(vl2.getValue()))) {
                    differences.add(new AttributeDifference("valueLink>value", vl1.getValue(), vl2.getValue()));
                }
                if ((vl1.getUnit() == null && vl2.getUnit() != null) || (vl1.getUnit() != null && vl2.getUnit() == null)
                        || (vl1.getUnit() != null && !vl1.getUnit().equals(vl2.getUnit()))) {
                    differences.add(new AttributeDifference("valueLink>unit", vl1.getUnit() != null ? vl1.getUnit().asText() : null, vl2.getUnit() != null ? vl2.getUnit().asText() : null));
                }
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
