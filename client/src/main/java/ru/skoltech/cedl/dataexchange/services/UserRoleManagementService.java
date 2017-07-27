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

import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.users.model.*;

import java.util.List;
import java.util.Map;

/**
 * Operations with {@link UserRoleManagement}
 *
 * Created by Nikolay Groshkov on 26-Jul-17.
 */
public interface UserRoleManagementService {

    /**
     * Retrieve an <i>admin</i> {@link Discipline} from {@link UserRoleManagement}.
     * If there is no one, then exception is thrown due to inconsistency.
     *
     * @param userRoleManagement {@link UserRoleManagement} for search in
     * @return <i>admin</i> {@link Discipline} founded.
     */
    Discipline obtainAdminDiscipline(UserRoleManagement userRoleManagement);

    /**
     * Build a {@link Map} of current {@link UserRoleManagement} disciplines (
     * discipline's name as a key).
     *
     * @param userRoleManagement {@link UserRoleManagement} for build from
     * @return map with discipline names as keys and discipline itself as values.
     */
    Map<String, Discipline> disciplineMap(UserRoleManagement userRoleManagement);

    /**
     * Remove {@link Discipline} from {@link UserRoleManagement}.
     *
     * @param userRoleManagement {@link UserRoleManagement} to remove from
     * @param discipline discipline to remove
     */
    void removeDiscipline(UserRoleManagement userRoleManagement, Discipline discipline);

    /**
     * Create and add {@link UserDiscipline} to {@link UserRoleManagement}.
     *
     * @param userRoleManagement {@link UserRoleManagement} to add in
     * @param user {@link User} to build {@link UserDiscipline}
     * @param discipline {@link Discipline} to build {@link UserDiscipline}
     * @return <i>true</i> if {@link UserDiscipline} was added successfully (not existent before),
     * <i>false</i> if opposite
     */
    boolean addUserDiscipline(UserRoleManagement userRoleManagement, User user, Discipline discipline);

    /**
     * Create and add {@link UserDiscipline} with <i>admin</i> {@link Discipline} to {@link UserRoleManagement}.
     *
     * @param userRoleManagement {@link UserRoleManagement} to add in
     * @param user {@link User} to build {@link UserDiscipline}
     * @return <i>true</i> if {@link UserDiscipline} was added successfully (not existent before),
     * <i>false</i> if opposite
     */
    boolean addAdminDiscipline(UserRoleManagement userRoleManagement, User user);

    /**
     * Retrieve a list of {@link User}s with particular {@link Discipline}.
     *
     * @param userRoleManagement {@link UserRoleManagement} for search in
     * @param discipline {@link Discipline} to search for
     * @return list of {@link User}s with specified {@link Discipline}
     */
    List<User> obtainUsersOfDiscipline(UserRoleManagement userRoleManagement, Discipline discipline);

    /**
     * Retrieve a list of {@link Discipline}s appointed to the {@link User}.
     *
     * @param userRoleManagement {@link UserRoleManagement} for search in
     * @param user {@link User} to search for
     * @return list of {@link Discipline}s appointed to the {@link User}
     */
    List<Discipline> obtainDisciplinesOfUser(UserRoleManagement userRoleManagement, User user);

    /**
     * Create and add {@link DisciplineSubSystem} to {@link UserRoleManagement}.
     *
     * @param userRoleManagement {@link UserRoleManagement} to add in
     * @param discipline {@link Discipline} to build {@link DisciplineSubSystem}
     * @param subSystem {@link SubSystemModel} to build {@link DisciplineSubSystem}
     * @return <i>true</i> if {@link DisciplineSubSystem} was added successfully (not existent before),
     * <i>false</i> if opposite
     */
    boolean addDisciplineSubsystem(UserRoleManagement userRoleManagement, Discipline discipline, SubSystemModel subSystem);

    /**
     * Retrieve a list of {@link SubSystemModel}s related to the specified {@link Discipline}.
     *
     * @param userRoleManagement {@link UserRoleManagement} for search in
     * @param discipline {@link Discipline} to search for
     * @return list of {@link SubSystemModel} related to the specified {@link Discipline}
     */
    List<SubSystemModel> obtainSubSystemsOfDiscipline(UserRoleManagement userRoleManagement, Discipline discipline);

    /**
     * Retrieve a list of {@link Discipline}s related to the specified {@link ModelNode}.
     *
     * @param userRoleManagement {@link UserRoleManagement} for search in
     * @param modelNode {@link ModelNode} to search for
     * @return list of {@link Discipline} related to the specified {@link ModelNode}
     */
    Discipline obtainDisciplineOfSubSystem(UserRoleManagement userRoleManagement, ModelNode modelNode);

    /**
     * Check if {@link User} is with <i>admin</i> rights in the specified {@link UserRoleManagement}.
     *
     * @param userRoleManagement {@link UserRoleManagement} to check in
     * @param user checked {@link User}
     * @return <i>true</i> if {@link User} has an <i>admin</i> rights, <i>false</i> if opposite
     */
    boolean checkUserAdmin(UserRoleManagement userRoleManagement, User user);

    /**
     * Check if {@link User} has rights to edit a {@link ModelNode} in the specified {@link UserRoleManagement}.
     * 
     * @param userRoleManagement {@link UserRoleManagement} to check in
     * @param user checked {@link User}
     * @param modelNode checked {@link ModelNode}
     * @return <i>true</i> if {@link User} has rights to edit {@link ModelNode}, <i>false</i> if opposite
     */
    boolean checkUserAccessToModelNode(UserRoleManagement userRoleManagement, User user, ModelNode modelNode);
}
