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

package ru.skoltech.cedl.dataexchange.structure.model.diff;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Utils;
import ru.skoltech.cedl.dataexchange.entity.ext.ExcelExternalModel;
import ru.skoltech.cedl.dataexchange.entity.ExternalModel;
import ru.skoltech.cedl.dataexchange.entity.PersistedEntity;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Created by D.Knoll on 19.05.2017.
 */
public class ExternalModelDifference extends ModelDifference {

    private static final Logger logger = Logger.getLogger(ExternalModelDifference.class);

    private ModelNode parent;

    private ExternalModel externalModel1;

    private ExternalModel externalModel2;

    public ExternalModelDifference(ModelNode parent, ExternalModel externalModel1, String name, ChangeType changeType, ChangeLocation changeLocation) {
        this.parent = parent;
        this.externalModel1 = externalModel1;
        this.attribute = name;
        this.changeType = changeType;
        this.changeLocation = changeLocation;
    }

    public ExternalModelDifference(ExternalModel externalModel1, ExternalModel externalModel2, String name,
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

    @Override
    public void mergeDifference() {
        if (changeLocation != ChangeLocation.ARG2) // handling only remote changes
            throw new IllegalStateException("local difference can not be merged");

        switch (changeType) {
            case ADD: { // add node to local parent
                Objects.requireNonNull(parent);
                final List<ExternalModel> externalModels = parent.getExternalModels();
                // TODO: block changes that make the model inconsistent (name duplicates, ...)
                ExternalModel newExternalModel = null;
                try {
                    newExternalModel = (ExternalModel) BeanUtils.cloneBean(externalModel1);
                } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                    logger.error("Cannot clone external model: " + externalModel1.getNodePath(), e);
                    throw new IllegalStateException("Cannot clone external model: " + externalModel1.getNodePath(), e);
                }
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
