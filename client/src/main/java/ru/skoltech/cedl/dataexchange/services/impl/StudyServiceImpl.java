/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
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
