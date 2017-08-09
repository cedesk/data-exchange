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

package ru.skoltech.cedl.dataexchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.StudySettings;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.repository.StudyRepository;
import ru.skoltech.cedl.dataexchange.services.StudyService;
import ru.skoltech.cedl.dataexchange.services.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.structure.BasicSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilder;

import java.util.List;

/**
 * Created by dknoll on 23/05/15.
 */
public class StudyStorageTest extends AbstractApplicationContextTest {

    private StudyService studyService;
    private UserRoleManagementService userRoleManagementService;
    private SystemBuilder systemBuilder;
    private StudyRepository studyRepository;

    @Before
    public void prepare() {
        studyService = context.getBean(StudyService.class);
        userRoleManagementService = context.getBean(UserRoleManagementService.class);
        systemBuilder = context.getBean(BasicSpaceSystemBuilder.class);
        studyRepository = context.getBean(StudyRepository.class);
    }

    @Test
    public void testStoreAndListStudies() {
        String name1 = "testStudy-1";
        Study study1 = makeStudy(name1, 1);
        studyRepository.saveAndFlush(study1);

        String name2 = "testStudy-2";
        Study study2 = makeStudy(name2, 1);
        studyRepository.saveAndFlush(study2);

        String name3 = "testStudy-3";
        Study study3 = makeStudy(name3, 1);
        studyRepository.saveAndFlush(study3);

        String[] createdStudies = new String[]{name1, name2, name3};
        List<String> storedStudyNames = studyRepository.findAllNames();

        Assert.assertArrayEquals(createdStudies, storedStudyNames.toArray());
    }

    @Test
    public void testStoreAndRetrieveStudy() {
        String name = "testStudy";
        Study study = makeStudy(name, 2);
        System.out.println(study);

        Study study0 = studyRepository.saveAndFlush(study);

        Study study1 = studyRepository.findByName(name);
        System.out.println(study1);

        Assert.assertEquals(study.getId(), study1.getId());
        Assert.assertEquals(study.getName(), study1.getName());
        Assert.assertEquals(study.getVersion(), study1.getVersion());

        Assert.assertEquals(study.getSystemModel(), study1.getSystemModel());

        UserRoleManagement urm1 = study.getUserRoleManagement();
        UserRoleManagement urm2 = study1.getUserRoleManagement();
        Assert.assertEquals(urm1, urm2);
        Assert.assertEquals(study, study1);
    }

    private Study makeStudy(String projectName, int modelDepth) {
        Study study = new Study();
        study.setStudySettings(new StudySettings());
        systemBuilder.modelDepth(modelDepth);
        SystemModel systemModel = systemBuilder.build("testModel");
        study.setSystemModel(systemModel);
        study.setName(projectName);
        UserRoleManagement userRoleManagement
                = userRoleManagementService.createUserRoleManagementWithSubsystemDisciplines(systemModel, null);
        study.setUserRoleManagement(userRoleManagement);
        studyService.relinkStudySubSystems(study);
        return study;
    }
}
