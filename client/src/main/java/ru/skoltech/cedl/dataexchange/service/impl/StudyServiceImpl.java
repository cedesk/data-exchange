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

package ru.skoltech.cedl.dataexchange.service.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.envers.RevisionType;
import org.springframework.beans.factory.annotation.Autowired;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.StudySettings;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.revision.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.entity.user.DisciplineSubSystem;
import ru.skoltech.cedl.dataexchange.entity.user.UserManagement;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.repository.envers.StudyRevisionRepository;
import ru.skoltech.cedl.dataexchange.repository.jpa.RevisionEntityRepository;
import ru.skoltech.cedl.dataexchange.repository.revision.StudyRepository;
import ru.skoltech.cedl.dataexchange.service.StudyService;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of {@link StudyService}.
 * <p>
 * Created by dknoll on 25/05/15.
 */
public class StudyServiceImpl implements StudyService {

    private final StudyRepository studyRepository;
    private final StudyRevisionRepository studyRevisionRepository;
    private final RevisionEntityRepository revisionEntityRepository;
    private UserRoleManagementService userRoleManagementService;

    @Autowired
    public StudyServiceImpl(StudyRepository studyRepository, StudyRevisionRepository studyRevisionRepository, RevisionEntityRepository revisionEntityRepository) {
        this.studyRepository = studyRepository;
        this.studyRevisionRepository = studyRevisionRepository;
        this.revisionEntityRepository = revisionEntityRepository;
    }

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
    public void deleteAllStudies() {
        studyRepository.deleteAll();
    }

    @Override
    public void deleteStudyByName(String studyName) {
        Study study = studyRepository.findByName(studyName);
        studyRepository.delete(study);
//        TODO: pass directly to custom and use
//        studyRepository.deleteByName(studyName);
    }

    @Override
    public List<Pair<CustomRevisionEntity, RevisionType>> findAllStudyRevisionEntityWithTags(Study study) {
        return revisionEntityRepository.findTaggedRevisions(study.getId(), Study.class);
    }

    @Override
    public String findCurrentStudyRevisionTag(Study study) {
        long studyId = study.getId();
        if (studyId == 0) return ""; // quick return for unstored studies
        CustomRevisionEntity revisionEntity = revisionEntityRepository.lastCustomRevisionEntity(studyId, Study.class);
        return revisionEntity.getTag();
    }

    @Override
    public Long findLatestModelModificationByStudyName(String studyName) {
        return studyRepository.findLatestModelModificationByName(studyName);
    }

    @Override
    public Study findStudyByName(String studyName) {
        return studyRepository.findByName(studyName);
    }

    @Override
    public Study findStudyByRevision(Study study, Integer revisionNumber) {
//        Revision<Integer, Study> revision = studyRevisionRepository.findRevision(study.getId(), revisionNumber);
        return studyRevisionRepository.findStudyByRevision(study.getId(), revisionNumber);
    }

    @Override
    public List<String> findStudyNames() {
        return studyRepository.findAllNames();
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

    @Override
    public Study saveStudy(Study study) {
        return studyRepository.saveAndFlush(study);
    }

    @Override
    public Study saveStudy(Study study, String tag) {
        return studyRepository.saveAndFlush(study, tag);
    }

    @Override
    public void tagStudy(Study study, String tag) {
        CustomRevisionEntity revisionEntity = revisionEntityRepository.lastCustomRevisionEntity(study.getId(), Study.class);
        revisionEntity.setTag(tag);
        revisionEntityRepository.saveAndFlush(revisionEntity);
    }

    @Override
    public void untagStudy(Study study) {
        CustomRevisionEntity revisionEntity = revisionEntityRepository.lastCustomRevisionEntity(study.getId(), Study.class);
        revisionEntity.setTag(null);
        revisionEntityRepository.saveAndFlush(revisionEntity);
    }

    @Override
    public void untagStudy(Study study, String tag) {
        List<Pair<CustomRevisionEntity, RevisionType>> allTagged = this.findAllStudyRevisionEntityWithTags(study);
        List<Pair<CustomRevisionEntity, RevisionType>> taggedWithTag = allTagged.stream()
                .filter(triple -> triple.getLeft().getTag().equals(tag))
                .collect(Collectors.toList());

        taggedWithTag.forEach(triple -> {
            CustomRevisionEntity revisionEntity = triple.getLeft();
            revisionEntity.setTag(null);
            revisionEntityRepository.saveAndFlush(revisionEntity);
        });
    }

}
