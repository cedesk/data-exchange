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

package ru.skoltech.cedl.dataexchange.services;

import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.user.UserManagement;

import java.util.List;

/**
 * Operations with {@link Study}.
 *
 * Created by Nikolay Groshkov on 06-Jul-17.
 */
public interface StudyService {

    /**
     * Create {@link Study} based on {@link SystemModel} and {@link UserManagement}.
     *
     * @param systemModel system model to base on
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
     * @param tag for tag new revision
     * @return the saved study
     */
    Study saveStudy(Study study, String tag);

    /**
     * Remove a {@link Study} with a specified name.
     * @param studyName name of the {@link Study}
     */
    void deleteStudyByName(String studyName);

    /**
     * Remove all stored studies.
     */
    void deleteAllStudies();

    /**
     * Retrieve a latest model modification timestamp of study with specified name.
     *
     * @param studyName name of the study.
     * @return timestamp value of latest model modification time.
     */
    Long findLatestModelModificationByStudyName(String studyName);

    /**
     * TODO add javadoc
     *
     * @param study study
     */
    void relinkStudySubSystems(Study study);
}
