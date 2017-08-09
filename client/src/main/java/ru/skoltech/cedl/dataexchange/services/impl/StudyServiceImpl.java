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

package ru.skoltech.cedl.dataexchange.services.impl;

import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.user.DisciplineSubSystem;
import ru.skoltech.cedl.dataexchange.services.StudyService;
import ru.skoltech.cedl.dataexchange.services.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.StudySettings;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.user.UserManagement;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by dknoll on 25/05/15.
 */
public class StudyServiceImpl implements StudyService {

    private UserRoleManagementService userRoleManagementService;

    public void setUserRoleManagementService(UserRoleManagementService userRoleManagementService) {
        this.userRoleManagementService = userRoleManagementService;
    }

    @Override
    public Study createStudy(SystemModel systemModel, UserManagement userManagement) {
        Study study = new Study();
        study.setStudySettings(new StudySettings());
        study.setSystemModel(systemModel);
        study.setName(systemModel.getName());
        UserRoleManagement userRoleManagement =
                userRoleManagementService.createUserRoleManagementWithSubsystemDisciplines(systemModel, userManagement);
        study.setUserRoleManagement(userRoleManagement);
        this.relinkStudySubSystems(study);
        return study;
    }


    @Override
    public void relinkStudySubSystems(Study study) {
        UserRoleManagement userRoleManagement = study.getUserRoleManagement();
        SystemModel systemModel = study.getSystemModel();

        if (userRoleManagement == null) {
            return;
        }
        List<DisciplineSubSystem> disciplineSubSystems = userRoleManagement.getDisciplineSubSystems();
        // build a map of the subsystems of the systemModel by UUID
        Map<String, SubSystemModel> subsystems
                = systemModel.getSubNodes().stream().collect(Collectors.toMap(SubSystemModel::getUuid, Function.identity()));
        // lookup subsystem by UUID
        disciplineSubSystems.forEach(disciplineSubSystem
                -> disciplineSubSystem.setSubSystem(subsystems.get(disciplineSubSystem.getSubSystem().getUuid())));
        // remove invalid links
        disciplineSubSystems.removeIf(disciplineSubSystem
                -> disciplineSubSystem.getSubSystem() == null);
    }

}
