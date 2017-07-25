/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.model.diff;

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.structure.model.ExternalModel;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.PersistedEntity;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 19.05.2017.
 */
public class ExternalModelDifference extends ModelDifference {

    private static final Logger logger = Logger.getLogger(ExternalModelDifference.class);

    private ModelNode parent;

    private ExternalModel externalModel1;

    private ExternalModel externalModel2;

    private ExternalModelDifference(ModelNode parent, ExternalModel externalModel1, String name, ChangeType changeType, ChangeLocation changeLocation) {
        this.parent = parent;
        this.externalModel1 = externalModel1;
        this.attribute = name;
        this.changeType = changeType;
        this.changeLocation = changeLocation;
    }

    private ExternalModelDifference(ExternalModel externalModel1, ExternalModel externalModel2, String name,
                                    ChangeType changeType, ChangeLocation changeLocation,
                                    String value1, String value2) {
        this.parent = externalModel1.getParent();
        this.externalModel1 = externalModel1;
        this.externalModel2 = externalModel2;
        this.attribute = name;
        this.changeType = changeType;
        this.changeLocation = changeLocation;
        this.value1 = value1;
        this.value2 = value2;
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

    @Override
    public String getElementPath() {
        return externalModel1.getNodePath();
    }

    public ExternalModel getExternalModel1() {
        return externalModel1;
    }

    @Override
    public ModelNode getParentNode() {
        return externalModel1.getParent();
    }

    @Override
    public boolean isMergeable() {
        return changeLocation == ChangeLocation.ARG2;
    }

    @Override
    public boolean isRevertible() {
        return changeLocation == ChangeLocation.ARG1;
    }

    public static ExternalModelDifference createRemoveExternalModel(ModelNode parent, ExternalModel externalModel1, String name, ChangeLocation changeLocation) {
        return new ExternalModelDifference(parent, externalModel1, name, ChangeType.REMOVE, changeLocation);
    }

    public static ExternalModelDifference createAddExternalModel(ModelNode parent, ExternalModel externalModel1, String name, ChangeLocation changeLocation) {
        return new ExternalModelDifference(parent, externalModel1, name, ChangeType.ADD, changeLocation);
    }

    public static ExternalModelDifference createExternalModelModified(ExternalModel externalModel1, ExternalModel externalModel2, String name) {
        boolean e2newer = externalModel2.getLastModification() > externalModel1.getLastModification();
        ChangeLocation changeLocation = e2newer ? ChangeLocation.ARG2 : ChangeLocation.ARG1;
        return new ExternalModelDifference(externalModel1, externalModel2, name, ChangeType.MODIFY, changeLocation, "", "");
    }

    public static ExternalModelDifference createExternalModelModified(ExternalModel externalModel1, ExternalModel externalModel2, String name, String value1, String value2) {
        boolean n2newer = externalModel2.isNewerThan(externalModel1);
        ChangeLocation changeLocation = n2newer ? ChangeLocation.ARG2 : ChangeLocation.ARG1;
        return new ExternalModelDifference(externalModel1, externalModel2, name, ChangeType.MODIFY, changeLocation, value1, value2);
    }

    public static List<ModelDifference> computeDifferences(ModelNode m1, ModelNode m2, Long latestStudy1Modification) {
        LinkedList<ModelDifference> extModelDifferences = new LinkedList<>();
        Map<String, ExternalModel> m1extModels = m1.getExternalModels().stream().collect(
                Collectors.toMap(ExternalModel::getUuid, Function.identity())
        );
        Map<String, ExternalModel> m2extModels = m2.getExternalModels().stream().collect(
                Collectors.toMap(ExternalModel::getUuid, Function.identity())
        );
        Set<String> allExtMods = new HashSet<>();
        allExtMods.addAll(m1extModels.keySet());
        allExtMods.addAll(m2extModels.keySet());

        for (String extMod : allExtMods) {
            ExternalModel e1 = m1extModels.get(extMod);
            ExternalModel e2 = m2extModels.get(extMod);

            if (e1 != null && e2 == null) {
                //if (e1.getLastModification() == null) { // model 1 was newly added
                extModelDifferences.add(createAddExternalModel(m1, e1, e1.getName(), ChangeLocation.ARG1));
                //} else { // model 2 was deleted
                //    extModelDifferences.add(createRemoveExternalModel(m1, e1, e1.getName(), ChangeLocation.ARG2));
                //}
            } else if (e1 == null && e2 != null) {
                Objects.requireNonNull(e2.getLastModification(), "persisted parameters always should have the timestamp set");
                if (e2.getLastModification() > latestStudy1Modification) { // model 2 was added
                    extModelDifferences.add(createAddExternalModel(m1, e2, e2.getName(), ChangeLocation.ARG2));
                } else { // model 1 was deleted
                    extModelDifferences.add(createRemoveExternalModel(m1, e2, e2.getName(), ChangeLocation.ARG1));
                }
            } else if (e1 != null && e2 != null) {
                if (!e1.getName().equals(e2.getName())) {
                    String value1 = e1.getName();
                    String value2 = e2.getName();
                    extModelDifferences.add(createExternalModelModified(e1, e2, "name", value1, value2));
                }
                if (!Arrays.equals(e1.getAttachment(), e2.getAttachment())) {
                    extModelDifferences.add(createExternalModelModified(e1, e2, "attachment"));
                }
            }
        }
        return extModelDifferences;
    }

    @Override
    public void mergeDifference() {
        if (changeLocation != ChangeLocation.ARG2) // handling only remote changes
            throw new IllegalStateException("local difference can not be merged");

        switch (changeType) {
            case ADD: { // add node to local parent
                Objects.requireNonNull(parent);
                final List<ExternalModel> externalModels = parent.getExternalModels();
                // TODO: block changes that make the model inconsistent (name duplicates, ...)
                ExternalModel newExternalModel = new ExternalModel();
                Utils.copyBean(externalModel1, newExternalModel);
                parent.addExternalModel(newExternalModel);
                break;
            }
            case REMOVE: { // remove node from local parent
                Objects.requireNonNull(parent);
                final String uuid = externalModel1.getUuid();
                final List<ExternalModel> externalModels = parent.getExternalModels();
                // TODO: block changes that make the model inconsistent (links to this parameter, ...)
                boolean removed = externalModels.removeIf(em -> em.getUuid().equals(uuid));
                if (!removed) {
                    logger.warn("external model to remove not present: " + externalModel1.getNodePath());
                } else {
                    // this avoids Hibernate to check list changes with persisted bags and try to replicate deletes in DB which are no longer there
                    parent.setExternalModels(new LinkedList<>(externalModels));
                }
                break;
            }
            case MODIFY: { // copy remote over local
                Objects.requireNonNull(externalModel1);
                Objects.requireNonNull(externalModel2);
                Utils.copyBean(externalModel2, externalModel1);
                externalModel1.setParent(parent); // link new item to actual new parent
                break;
            }
            default: {
                logger.error("MERGE IMPOSSIBLE:\n" + toString());
                throw new NotImplementedException();
            }
        }
    }

    @Override
    public void revertDifference() {
        if (changeLocation != ChangeLocation.ARG1)
            throw new IllegalStateException("non-local difference can not be reverted");

        final String uuid = externalModel1.getUuid();
        switch (changeType) {
            case ADD: { // remove local again
                Objects.requireNonNull(parent);
                List<ExternalModel> externalModels = parent.getExternalModels();
                // TODO: block changes that make the model inconsistent (links to this parameter, ...)
                boolean removed = externalModels.removeIf(em -> em.getUuid().equals(uuid));
                if (!removed) {
                    logger.warn("external model to remove not present: " + externalModel1.getNodePath());
                } else {
                    // this avoids Hibernate to check list changes with persisted bags and try to replicate deletes in DB which are no longer there
                    parent.setExternalModels(new LinkedList<>(externalModels));
                }
                break;
            }
            case REMOVE: { // re-add local again
                Objects.requireNonNull(parent);
                if (parent.getExternalModelMap().containsKey(externalModel1.getName())) {
                    logger.error("unable to re-add parameter, because another external model of same name is already there");
                } else {
                    parent.addExternalModel(externalModel1);
                }
                break;
            }
            case MODIFY: { // copy remote over local
                Objects.requireNonNull(externalModel1);
                Objects.requireNonNull(externalModel2);
                Utils.copyBean(externalModel2, externalModel1);
                externalModel1.setParent(parent); // link new item to actual new parent
                break;
            }
            default: {
                logger.error("MERGE IMPOSSIBLE:\n" + toString());
                throw new NotImplementedException();
            }
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
}
