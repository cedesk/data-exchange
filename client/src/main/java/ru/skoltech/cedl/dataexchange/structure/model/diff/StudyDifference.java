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

import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.PersistedEntity;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.services.StudyService;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by D.Knoll on 12.05.2016.
 */
public class StudyDifference extends ModelDifference {

    private static final Logger logger = Logger.getLogger(StudyDifference.class);

    protected Study study1;
    protected Study study2;

    private StudyService studyService;

    public StudyDifference(Study study1, Study study2, ChangeType changeType, ChangeLocation changeLocation,
                           String attribute, String value1, String value2, StudyService studyService) {
        this.study1 = study1;
        this.study2 = study2;
        this.attribute = attribute;
        this.changeType = changeType;
        this.changeLocation = changeLocation;
        this.value1 = value1;
        this.value2 = value2;

        this.studyService = studyService;
    }

    @Override
    public PersistedEntity getChangedEntity() {
        if (changeType == ChangeType.MODIFY) {
            return changeLocation == ChangeLocation.ARG1 ? study1 : study2;
        } else {
            throw new IllegalArgumentException("Unknown change type and location combination");
        }
    }

    @Override
    public String getElementPath() {
        return study1.getName();
    }

    @Override
    public ModelNode getParentNode() {
        return study1.getSystemModel();
    }

    @Override
    public boolean isMergeable() {
        return changeType == ChangeType.MODIFY && changeLocation == ChangeLocation.ARG2;
    }

    @Override
    public boolean isRevertible() {
        //TODO implement
        return false;
    }

    @Override
    public void mergeDifference() {
        if (changeType == ChangeType.MODIFY && changeLocation == ChangeLocation.ARG2) {
            study1.setVersion(study2.getVersion());
            study1.setUserRoleManagement(study2.getUserRoleManagement());
            studyService.relinkStudySubSystems(study1);
            study1.setStudySettings(study2.getStudySettings());
        } else {
            logger.error("MERGE IMPOSSIBLE:\n" + toString());
        }
    }

    @Override
    public void revertDifference() {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StudyDifference{");
        sb.append("attribute='").append(attribute).append('\'');
        sb.append(", changeType=").append(changeType);
        sb.append(", changeLocation=").append(changeLocation);
        sb.append(", value1='").append(value1).append('\'');
        sb.append(", value2='").append(value2).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append(", study1=").append(study1);
        sb.append(", study2=").append(study2);
        sb.append('}');
        return sb.toString();
    }
}
