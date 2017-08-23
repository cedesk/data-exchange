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

package ru.skoltech.cedl.dataexchange.service;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.envers.RevisionType;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.revision.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.entity.user.UserManagement;

import java.util.Date;
import java.util.List;

/**
 * Operations with {@link Study}.
 * <p>
 * Created by Nikolay Groshkov on 06-Jul-17.
 */
public interface StudyService {

    /**
     * Create {@link Study} based on {@link SystemModel} and {@link UserManagement}.
     *
     * @param systemModel    system model to base on
     * @param userManagement user management to base on
     * @return new instance of {@link Study}
     */
    Study createStudy(SystemModel systemModel, UserManagement userManagement);

    /**
     * Retrieve names of all studies.
     *
     * @return list of studies names
     */
    List<String> findStudyNames();

    /**
     * Retrieve a {@link Study} by its name.
     *
     * @param studyName name of the {@link Study}
     * @return instance of the {@link Study}
     */
    Study findStudyByName(String studyName);

    /**
     * Saves an study.
     *
     * @param study study to save
     * @return the saved study
     */
    Study saveStudy(Study study);

    /**
     * Saves an study and tag produced revision.
     *
     * @param study study to save
     * @param tag   for tag new revision
     * @return the saved study
     */
    Study saveStudy(Study study, String tag);

    /**
     * Retrieve a tag of current study revision.
     *
     * @param study study to find a tag
     * @return tag  of current study revision
     */
    String findCurrentStudyRevisionTag(Study study);

    /**
     * Retrieve all revision entities which was tagged and saved along with a specified study.
     *
     * @param study study which tagged revision entities to search
     * @return a list of pair objects each of which stores a tagged instance of {@link CustomRevisionEntity}
     * revision entity and revision type.
     */
    List<Pair<CustomRevisionEntity, RevisionType>> findAllStudyRevisionEntityWithTags(Study study);

    /**
     * Tag a current revision of a study.
     *
     * @param study study which current revision to tag
     * @param tag   for tag new revision
     */
    void tagStudy(Study study, String tag);

    /**
     * Clean tag from current revision of a study.
     *
     * @param study study from which current revision to clean tag
     */
    void untagStudy(Study study);

    /**
     * Clean tag of revision entity of specified {@link Study} which has a specified tag.
     *
     * @param study of which tagged revision entity to search for
     * @param tag   tag to clean
     */
    void untagStudy(Study study, String tag);

    /**
     * Retrieve an instance of {@link Study} from it revision history by revision number.
     *
     * @param study          which revision to search for
     * @param revisionNumber revision number of searched {@link Study}
     * @return Study from revision
     */
    Study findStudyByRevision(Study study, Integer revisionNumber);

    /**
     * Remove a {@link Study} with a specified name.
     *
     * @param studyName name of the {@link Study}
     */
    void deleteStudyByName(String studyName);

    /**
     * Remove all stored studies.
     */
    void deleteAllStudies();

    /**
     * Retrieve a latest revision number along with revision date
     * of study with specified id.
     *
     * @param studyId id of the study
     * @return latest revision number of study
     */
    Pair<Integer, Date> findLatestRevision(Long studyId);

    /**
     * Retrieve a latest revision number of study with specified id.
     *
     * @param studyId id of the study
     * @return latest revision number of study
     */
    Integer findLatestRevisionNumber(Long studyId);

    /**
     * TODO add javadoc
     *
     * @param study study
     */
    void relinkStudySubSystems(Study study);
}
