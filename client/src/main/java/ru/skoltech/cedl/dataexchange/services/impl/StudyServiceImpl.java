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

import ru.skoltech.cedl.dataexchange.services.StudyService;
import ru.skoltech.cedl.dataexchange.services.UserManagementService;
import ru.skoltech.cedl.dataexchange.structure.model.Study;
import ru.skoltech.cedl.dataexchange.structure.model.StudySettings;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Created by dknoll on 25/05/15.
 */
public class StudyServiceImpl implements StudyService {

    private UserManagementService userManagementService;

    public void setUserManagementService(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @Override
    public Study createStudy(SystemModel systemModel, UserManagement userManagement) {
        Study study = new Study();
        study.setStudySettings(new StudySettings());
        study.setSystemModel(systemModel);
        study.setName(systemModel.getName());
        UserRoleManagement userRoleManagement =
                userManagementService.createUserRoleManagementWithSubsystemDisciplines(systemModel, userManagement);
        study.setUserRoleManagement(userRoleManagement);
        return study;
    }
}
