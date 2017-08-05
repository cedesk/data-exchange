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

import ru.skoltech.cedl.dataexchange.db.CustomRevisionEntity;
import ru.skoltech.cedl.dataexchange.logging.LogEntry;
import ru.skoltech.cedl.dataexchange.repository.RepositoryException;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.units.model.UnitManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Operation with repository.
 *
 * Created by D.Knoll on 25.05.2015.
 */
public interface RepositoryService {

    List<String> listStudies() throws RepositoryException;

    Study loadStudy(String name) throws RepositoryException;

    @Transactional
    void deleteStudy(String name) throws RepositoryException;

    @Transactional
    Study storeStudy(Study study) throws RepositoryException;

    SystemModel loadSystemModel(long studyId) throws RepositoryException;

    @Transactional
    SystemModel storeSystemModel(SystemModel systemModel) throws RepositoryException;

    ExternalModel loadExternalModel(long externalModelId) throws RepositoryException;

    @Transactional
    ExternalModel storeExternalModel(ExternalModel externalModel) throws RepositoryException;

    UserRoleManagement loadUserRoleManagement(long studyId) throws RepositoryException;

    @Transactional
    UserRoleManagement storeUserRoleManagement(UserRoleManagement userRoleManagement) throws RepositoryException;

    UserManagement loadUserManagement() throws RepositoryException;

    @Transactional
    UserManagement storeUserManagement(UserManagement userManagement) throws RepositoryException;

    UnitManagement loadUnitManagement() throws RepositoryException;

    @Transactional
    UnitManagement storeUnitManagement(UnitManagement unitManagement) throws RepositoryException;

    @Transactional
    List<ParameterRevision> getChangeHistory(ParameterModel parameterModel) throws RepositoryException;

    CustomRevisionEntity getLastRevision(PersistedEntity persistedEntity);

    @Transactional
    void storeLog(LogEntry logEntry);

    Long getLastStudyModification(String name);

    String loadSchemeVersion()  throws RepositoryException;

    @Transactional
    boolean storeSchemeVersion(String schemeVersion) throws RepositoryException;

    @Transactional
    boolean checkAndStoreSchemeVersion();

    boolean checkSchemeVersion();
}
