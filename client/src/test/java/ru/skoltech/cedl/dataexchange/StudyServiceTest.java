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

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.hibernate.envers.RevisionType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.StudySettings;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.revision.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.init.AbstractApplicationContextTest;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.repository.jpa.RevisionEntityRepository;
import ru.skoltech.cedl.dataexchange.service.StudyService;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.structure.BasicSpaceSystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.SystemBuilder;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

/**
 * Created by dknoll on 23/05/15.
 */
public class StudyServiceTest extends AbstractApplicationContextTest {

    private ApplicationSettings applicationSettings;
    private StudyService studyService;
    private UserRoleManagementService userRoleManagementService;
    private SystemBuilder systemBuilder;
    private RevisionEntityRepository revisionEntityRepository;

    @Before
    public void prepare() {
        applicationSettings = context.getBean(ApplicationSettings.class);
        studyService = context.getBean(StudyService.class);
        userRoleManagementService = context.getBean(UserRoleManagementService.class);
        systemBuilder = context.getBean(BasicSpaceSystemBuilder.class);
        revisionEntityRepository = context.getBean(RevisionEntityRepository.class);
    }

    @After
    public void cleanup() {
        studyService.deleteAllStudies();
    }

    @Test
    public void testCreateStudy() {
        String systemModelName = "createdSystemModelName";
        SystemModel systemModel = systemBuilder.build(systemModelName);

        Study study = studyService.createStudy(systemModel, null);
        assertEquals(systemModelName, study.getName());
        assertEquals(0, study.getId());
        assertNotNull(study.getStudySettings());
        assertNotNull(study.getUserRoleManagement());

    }

    @Test(expected = AssertionError.class)
    public void testFindLatestRevisionException() {
        studyService.findLatestRevision(null);
    }

    @Test(expected = AssertionError.class)
    public void testFindLatestRevisionByNameException() {
        studyService.findLatestRevisionByName(null);
    }

    @Test(expected = AssertionError.class)
    public void findStudyByNameAndRevisionException1() {
        studyService.findStudyByNameAndRevision(null, 1);
    }

    @Test(expected = AssertionError.class)
    public void findStudyByNameAndRevisionException2() {
        studyService.findStudyByNameAndRevision("studyName", null);
    }

    @Test
    public void testFindLatestRevision() {
        String systemModelName = "systemModelName";
        SystemModel systemModel = systemBuilder.build(systemModelName);
        Study study = studyService.createStudy(systemModel, null);

        Pair<Integer, Date> revisionPair = studyService.findLatestRevision(study.getId());
        assertNull(revisionPair);
        Triple<Study, Integer, Date> revisionTriple = studyService.findLatestRevisionByName(study.getName());
        assertNull(revisionTriple);

        Triple<Study, Integer, Date> studyAndRevision = studyService.saveStudy(study);
        Study savedStudy = studyAndRevision.getLeft();
        revisionPair = studyService.findLatestRevision(savedStudy.getId());
        assertNotNull(revisionPair);
        revisionTriple = studyService.findLatestRevisionByName(savedStudy.getName());
        assertNotNull(revisionTriple);

        Study revisionStudy = studyService.findStudyByNameAndRevision(systemModelName, studyAndRevision.getMiddle());
        assertEquals(systemModel.getName(), revisionStudy.getName());
    }

    @Test
    public void testSaveAndListStudies() {
        String name1 = "testStudy-1";
        Study study1 = makeStudy(name1, 1);
        studyService.saveStudy(study1);

        String name2 = "testStudy-2";
        Study study2 = makeStudy(name2, 1);
        studyService.saveStudy(study2);

        String name3 = "testStudy-3";
        Study study3 = makeStudy(name3, 1);
        studyService.saveStudy(study3);

        String[] createdStudies = new String[]{name1, name2, name3};
        List<String> storedStudyNames = studyService.findStudyNames();

        assertArrayEquals(createdStudies, storedStudyNames.toArray());
    }

    @Test
    public void testSaveAndRetrieveStudy() {
        String name = "testStudy";
        Study studyPrototype = makeStudy(name, 2);
        System.out.println(studyPrototype);

        Triple<Study, Integer, Date> studyRevision = studyService.saveStudy(studyPrototype);
        Study study = studyRevision.getLeft();
        Study studyStored = studyService.findStudyByName(name);
        System.out.println(studyStored);

        assertEquals(studyPrototype, studyStored);
        assertEquals(studyPrototype.getId(), studyStored.getId());
        assertEquals(studyPrototype.getName(), studyStored.getName());
        assertEquals(studyPrototype.getVersion(), studyStored.getVersion());
        assertEquals(studyPrototype.getSystemModel(), studyStored.getSystemModel());
        assertEquals(studyPrototype.getUserRoleManagement(), studyStored.getUserRoleManagement());

        Pair<CustomRevisionEntity, RevisionType> revision = revisionEntityRepository.lastRevision(study.getId(), Study.class);
        assertEquals(applicationSettings.getProjectUserName(), revision.getLeft().getUsername());
        assertNull(revision.getLeft().getTag());
        assertEquals(RevisionType.ADD, revision.getRight());

        studyStored = studyService.findStudyByName(name);
        assertEquals(revision.getLeft().getId(), studyStored.getRevision());
    }

    @Test
    public void testTagSaveStudy() {
        String name = "testStudyTag";
        Study studyPrototype = makeStudy(name, 2);
        System.out.println(studyPrototype);

        String tag = "tag";

        Study study = studyService.saveStudy(studyPrototype, tag);

        Pair<CustomRevisionEntity, RevisionType> revision = revisionEntityRepository.lastRevision(study.getId(), Study.class);
        assertEquals(applicationSettings.getProjectUserName(), revision.getLeft().getUsername());
        assertEquals(tag, revision.getLeft().getTag());
        assertEquals(RevisionType.ADD, revision.getRight());
    }

    @Test
    public void testDeleteStudy() {
        String name1 = "testStudyToDelete1";
        Study studyPrototype1 = makeStudy(name1, 2);

        String name2 = "testStudyToDelete2";
        Study studyPrototype2 = makeStudy(name2, 2);

        System.out.println(studyPrototype1 + ", " + studyPrototype2);

        Triple<Study, Integer, Date> revision1 = studyService.saveStudy(studyPrototype1);
        Triple<Study, Integer, Date> revision2 = studyService.saveStudy(studyPrototype2);

        Study study1 = revision1.getLeft();
        Study study2 = revision2.getLeft();

        Study studyStored1 = studyService.findStudyByName(name1);
        Study studyStored2 = studyService.findStudyByName(name2);

        assertEquals(studyPrototype1.getName(), studyStored1.getName());
        assertEquals(studyPrototype2.getName(), studyStored2.getName());

        studyService.deleteStudyByName(name1);
        studyStored1 = studyService.findStudyByName(name1);
        assertNull(studyStored1);
        assertThat(studyService.findStudyNames(), not(empty()));

        Pair<CustomRevisionEntity, RevisionType> deleteRevision1 = revisionEntityRepository.lastRevision(study1.getId(), Study.class);
        assertEquals(applicationSettings.getProjectUserName(), deleteRevision1.getLeft().getUsername());
        assertEquals(RevisionType.DEL, deleteRevision1.getRight());

        studyService.deleteAllStudies();
        assertThat(studyService.findStudyNames(), empty());

        Pair<CustomRevisionEntity, RevisionType> deleteRevision2 = revisionEntityRepository.lastRevision(study2.getId(), Study.class);
        assertEquals(applicationSettings.getProjectUserName(), deleteRevision2.getLeft().getUsername());
        assertEquals(RevisionType.DEL, deleteRevision2.getRight());
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
