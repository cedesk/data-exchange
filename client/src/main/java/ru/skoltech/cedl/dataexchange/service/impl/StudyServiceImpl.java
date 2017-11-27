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
import org.apache.commons.lang3.tuple.Triple;
import org.hibernate.envers.RevisionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.StudySettings;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.revision.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.entity.user.DisciplineSubSystem;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.repository.envers.StudyRevisionRepository;
import ru.skoltech.cedl.dataexchange.repository.jpa.RevisionEntityRepository;
import ru.skoltech.cedl.dataexchange.repository.revision.StudyRepository;
import ru.skoltech.cedl.dataexchange.service.StudyService;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of {@link StudyService}.
 * <p>
 * Created by dknoll on 25/05/15.
 */
public class StudyServiceImpl implements StudyService {

    private UserRoleManagementService userRoleManagementService;

    private final StudyRepository studyRepository;
    private final StudyRevisionRepository studyRevisionRepository;
    private final RevisionEntityRepository revisionEntityRepository;

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
    public Study createStudy(SystemModel systemModel) {
        Study study = new Study();
        study.setStudySettings(new StudySettings());
        study.setSystemModel(systemModel);
        study.setName(systemModel.getName());
        study.setVersion(0);
        UserRoleManagement userRoleManagement =
                userRoleManagementService.createUserRoleManagementWithSubsystemDisciplines(systemModel);
        study.setUserRoleManagement(userRoleManagement);
        this.relinkStudySubSystems(study);
        return study;
    }

    @Override
    public List<String> findStudyNames() {
        return studyRepository.findAllNames();
    }

    @Override
    public Study findStudyByName(String studyName) {
        return studyRepository.findByName(studyName);
    }

    @Override
    public Triple<Study, Integer, Date> saveStudy(Study study) {
        Study newStudy = studyRepository.saveAndFlush(study);
        Pair<Integer, Date> revision = this.findLatestRevision(newStudy.getId());
        return Triple.of(newStudy, revision.getLeft(), revision.getRight());
    }

    @Override
    public Study saveStudy(Study study, String tag) {
        return studyRepository.saveAndFlush(study, tag);
    }

    @Override
    public String findCurrentStudyRevisionTag(Study study) {
        long studyId = study.getId();
        if (studyId == 0) {
            return null;
        }
        CustomRevisionEntity revisionEntity = revisionEntityRepository.lastRevisionEntity(studyId, Study.class);
        return revisionEntity.getTag();
    }

    @Override
    public List<Pair<CustomRevisionEntity, RevisionType>> findAllStudyRevisionEntityWithTags(Study study) {
        return revisionEntityRepository.findTaggedRevisions(study.getId(), Study.class);
    }

    @Override
    public void tagStudy(Study study, String tag) {
        CustomRevisionEntity revisionEntity = revisionEntityRepository.lastRevisionEntity(study.getId(), Study.class);
        revisionEntity.setTag(tag);
        revisionEntityRepository.saveAndFlush(revisionEntity);
    }

    @Override
    public void untagStudy(Study study) {
        CustomRevisionEntity revisionEntity = revisionEntityRepository.lastRevisionEntity(study.getId(), Study.class);
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

    @Override
    public void deleteStudyByName(String studyName) {
        Study study = studyRepository.findByName(studyName);
        study.getUserRoleManagement().getDisciplineSubSystems().clear();
        study = studyRepository.saveAndFlush(study);
        studyRepository.delete(study);
    }

    @Override
    public void deleteAllStudies() {
        studyRepository.deleteAll();
    }

    @Override
    public Study findStudyByNameAndRevision(String studyName, Integer revisionNumber) {
        assert Objects.nonNull(studyName);
        assert Objects.nonNull(revisionNumber);

        Study study = findStudyByName(studyName);
        return studyRevisionRepository.findStudyByRevision(study.getId(), revisionNumber);
    }

    @Override
    public Triple<Study, Integer, Date> findLatestRevisionByName(String studyName) {
        assert Objects.nonNull(studyName);

        Study study = this.findStudyByName(studyName);
        if (study == null) {
            return null;
        }
        Pair<Integer, Date> revision = this.findLatestRevision(study.getId());
        return Triple.of(study, revision.getLeft(), revision.getRight());
    }

    @Override
    public Pair<Integer, Date> findLatestRevision(Long studyId) {
        assert Objects.nonNull(studyId);

        Revision<Integer, Study> revision = studyRevisionRepository.findLastChangeRevision(studyId);
        if (revision == null) {
            return null;
        }
        return Pair.of(revision.getRevisionNumber(), revision.getRevisionDate().toDate());
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
