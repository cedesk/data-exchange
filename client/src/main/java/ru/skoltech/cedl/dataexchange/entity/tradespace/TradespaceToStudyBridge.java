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

package ru.skoltech.cedl.dataexchange.entity.tradespace;

import ru.skoltech.cedl.dataexchange.db.RepositoryStateMachine;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.ParameterNature;
import ru.skoltech.cedl.dataexchange.entity.ParameterTreeIterator;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.service.impl.StudyServiceImpl;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by d.knoll on 6/28/2017.
 */
public class TradespaceToStudyBridge {

    private final Project project;
    private SystemModel systemModel;
    private Map<String, ParameterModel> parameterDictionary;
    private StudyServiceImpl studyService;
    private RepositoryStateMachine repositoryStateMachine;

    public TradespaceToStudyBridge(Project project) {
        this.project = project;
    }

    public String getCurrentRevisionName() {
        return studyService.findCurrentStudyRevisionTag(project.getStudy());
    }

    public Collection<ParameterModel> getModelOutputParameters() {
        List<ParameterModel> parameters = new LinkedList<>();
        ParameterTreeIterator subsystemParameterIterator = new ParameterTreeIterator(getSystemModel(),
                parameterModel -> parameterModel.getNature() == ParameterNature.OUTPUT);
        subsystemParameterIterator.forEachRemaining(parameters::add);
        return parameters;
    }

    private Map<String, ParameterModel> getParameterDictionary() {
        if (parameterDictionary == null) {
            parameterDictionary = getSystemModel().makeParameterDictionary();
        }
        return parameterDictionary;
    }

    public Long getStudyId() {
        return project.getStudy().getId();
    }

    private SystemModel getSystemModel() {
        if (systemModel == null) {
            systemModel = project.getSystemModel();
        }
        return systemModel;
    }

    public String getStudyName() {
        return project.getProjectName();
    }

    public boolean isSaved() {
        return repositoryStateMachine.hasModifications();
    }

    public void setRepositoryStateMachine(RepositoryStateMachine repositoryStateMachine) {
        this.repositoryStateMachine = repositoryStateMachine;
    }

    public String getParameterName(String parameterUuid) {
        if (parameterUuid == null || parameterUuid.isEmpty()) return "<not defined>";

        ParameterModel parameterModel = getParameterDictionary().get(parameterUuid);
        if (parameterModel == null) {
            return "<not found>";
        }
        return parameterModel.getNodePath();
    }

    public String getParameterUnitOfMeasure(String parameterUuid) {
        if (parameterUuid == null || parameterUuid.isEmpty()) return "<not defined>";

        ParameterModel parameterModel = getParameterDictionary().get(parameterUuid);
        if (parameterModel == null) {
            return "<not found>";
        }
        if (parameterModel.getUnit() == null) {
            return "<not defined>";
        }
        return parameterModel.getUnit().asText();
    }

    public Double getParameterValue(String parameterUuid) {
        if (parameterUuid == null) return null;

        ParameterModel parameterModel = getParameterDictionary().get(parameterUuid);
        if (parameterModel != null) {
            return parameterModel.getEffectiveValue();
        }
        return null;
    }

    public void setStudyService(StudyServiceImpl studyService) {
        this.studyService = studyService;
    }
}
