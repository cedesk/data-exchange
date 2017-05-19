package ru.skoltech.cedl.dataexchange.structure.model.diff;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.PersistedEntity;

import java.io.IOException;
import java.util.*;

/**
 * Created by D.Knoll on 17.09.2015.
 */
public class ExternalModelDifference extends ModelDifference {

    private static final Logger logger = Logger.getLogger(ExternalModelDifference.class);

    protected ExternalModel externalModel1;

    protected ExternalModel externalModel2;

    private ExternalModelDifference(ExternalModel externalModel1, String name, ChangeType changeType, ChangeLocation changeLocation) {
        this.externalModel1 = externalModel1;
        this.attribute = name;
        this.changeType = changeType;
        this.changeLocation = changeLocation;
    }

    private ExternalModelDifference(ExternalModel externalModel1, ExternalModel externalModel2, String name,
                                    ChangeType changeType, ChangeLocation changeLocation,
                                    String value1, String value2) {
        this.externalModel1 = externalModel1;
        this.externalModel2 = externalModel2;
        this.attribute = name;
        this.changeType = changeType;
        this.changeLocation = changeLocation;
        this.value1 = value1;
        this.value2 = value2;
    }

    public static ExternalModelDifference createRemoveExternalModel(ExternalModel externalModel1, String name, ChangeLocation changeLocation) {
        return new ExternalModelDifference(externalModel1, name, ChangeType.REMOVE, changeLocation);
    }

    public static ExternalModelDifference createAddExternalModel(ExternalModel externalModel1, String name, ChangeLocation changeLocation) {
        return new ExternalModelDifference(externalModel1, name, ChangeType.ADD, changeLocation);
    }

    public static ExternalModelDifference createExternalModelModified(ExternalModel externalModel1, ExternalModel externalModel2, String name, ChangeLocation changeLocation) {
        return new ExternalModelDifference(externalModel1, externalModel2, name, ChangeType.MODIFY, changeLocation, "", "");
    }

    static List<ModelDifference> differencesOnExternalModels(ModelNode m1, ModelNode m2, Long latestStudy1Modification) {
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
                if (e1.getLastModification() == null) { // model 1 was newly added
                    extModelDifferences.add(createAddExternalModel(e1, e1.getName(), ChangeLocation.ARG1));
                } else { // parameter 2 was deleted
                    extModelDifferences.add(createRemoveExternalModel(e1, e1.getName(), ChangeLocation.ARG1));
                }
            } else if (e1 == null && e2 != null) {
                if (e2.getLastModification() <= latestStudy1Modification) { // model 2 was deleted
                    extModelDifferences.add(createRemoveExternalModel(e2, e2.getName(), ChangeLocation.ARG1));
                } else { // model 1 was added
                    extModelDifferences.add(createAddExternalModel(e2, e2.getName(), ChangeLocation.ARG2));
                }
            } else if (e1 != null && e2 != null) {
                if (!Arrays.equals(e1.getAttachment(), e2.getAttachment())) {
                    boolean e2newer = e2.getLastModification() > e1.getLastModification();
                    ChangeLocation changeLocation = e2newer ? ChangeLocation.ARG2 : ChangeLocation.ARG1;
                    extModelDifferences.add(createExternalModelModified(e1, e2, e1.getName(), changeLocation));
                }
            }
        }
        return extModelDifferences;
    }

    @Override
    public ModelNode getParentNode() {
        return externalModel1.getParent();
    }

    @Override
    public String getNodeName() {
        return externalModel1.getNodePath();
    }

    @Override
    public String getParameterName() {
        return "";
    }

    @Override
    public boolean isMergeable() {
        return changeType == ChangeType.MODIFY;
    }

    @Override
    public void mergeDifference() {
        if (changeType == ChangeType.MODIFY) {
            Objects.requireNonNull(externalModel1);
            Objects.requireNonNull(externalModel2);
            Objects.requireNonNull(attribute);
            ModelNode node1 = externalModel1.getParent();
            ModelNode node2 = externalModel2.getParent();
            if (node1.getExternalModelMap().containsKey(attribute) && node2.getExternalModelMap().containsKey(attribute)) {
                ExternalModel fromExtMo = node2.getExternalModelMap().get(attribute);
                ExternalModel toExtMo = node1.getExternalModelMap().get(attribute);
                Utils.copyBean(fromExtMo, toExtMo);
                try {
                    ProjectContext.getInstance().getProject().getExternalModelFileHandler().forceCacheUpdate(toExtMo);
                } catch (IOException e) {
                    logger.error("failed to update cache for external model: " + toExtMo.getNodePath());
                }
            } else {
                logger.error("MERGE IMPOSSIBLE:\n" + toString());
            }
        } else {
            logger.error("MERGE IMPOSSIBLE:\n" + toString());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NodeDifference{");
        sb.append("node1='").append(externalModel1.getName()).append('\'');
        if (externalModel2 != null) {
            sb.append(", node2='").append(externalModel2.getName()).append('\'');
        }
        sb.append(", attribute='").append(attribute).append('\'');
        sb.append(", changeType=").append(changeType);
        sb.append(", changeLocation=").append(changeLocation);
        sb.append(", value1='").append(value1).append('\'');
        sb.append(", value2='").append(value2).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append("}\n ");
        return sb.toString();
    }

    @Override
    public PersistedEntity getChangedEntity() {
        if (changeType == ChangeType.MODIFY) {
            return changeLocation == ChangeLocation.ARG1 ? externalModel1 : externalModel2;
        } else if (changeType == ChangeType.ADD || changeType == ChangeType.REMOVE) {
            return externalModel1;
        } else {
            throw new IllegalArgumentException("Unknown change type and location combination");
        }
    }
}
