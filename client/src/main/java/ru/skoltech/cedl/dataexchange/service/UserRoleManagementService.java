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

import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.user.*;

import java.util.List;
import java.util.Map;

/**
 * Operations with {@link UserRoleManagement}
 * <p>
 * Created by Nikolay Groshkov on 26-Jul-17.
 */
public interface UserRoleManagementService {

    /**
     * Create and add {@link UserDiscipline} association with <i>admin</i> {@link Discipline} to {@link UserRoleManagement}.
     * Does not allow duplication.
     *
     * @param userRoleManagement {@link UserRoleManagement} to add in
     * @param user               {@link User} to build {@link UserDiscipline}
     * @return <i>false</i> if {@link UserDiscipline} was added successfully (not existent before),
     * <i>true</i> if opposite
     */
    boolean addAdminDiscipline(UserRoleManagement userRoleManagement, User user);

    /**
     * Create and add {@link DisciplineSubSystem} to {@link UserRoleManagement}.
     * Does not allow duplication.
     *
     * @param userRoleManagement {@link UserRoleManagement} to add in
     * @param discipline         {@link Discipline} to build {@link DisciplineSubSystem}
     * @param subSystem          {@link SubSystemModel} to build {@link DisciplineSubSystem}
     * @return <i>false</i> if {@link DisciplineSubSystem} was added successfully (not existent before),
     * <i>true</i> if opposite
     */
    boolean addDisciplineSubsystem(UserRoleManagement userRoleManagement, Discipline discipline, SubSystemModel subSystem);

    /**
     * Create and add {@link UserDiscipline} association to {@link UserRoleManagement}.
     * Does not allow duplication.
     *
     * @param userRoleManagement {@link UserRoleManagement} to add in
     * @param user               {@link User} to build {@link UserDiscipline}
     * @param discipline         {@link Discipline} to build {@link UserDiscipline}
     * @return <i>false</i> if {@link UserDiscipline} was added successfully (not existent before),
     * <i>true</i> if opposite
     */
    boolean addUserDiscipline(UserRoleManagement userRoleManagement, User user, Discipline discipline);

    /**
     * Add user with specified name and set an administrator role to him.
     *
     * @param userRoleManagement {@link UserRoleManagement} to add on
     * @param userManagement     {@link UserManagement} to add on
     * @param userName           name of new {@link User}
     */
    void addUserWithAdminRole(UserRoleManagement userRoleManagement, UserManagement userManagement, String userName);

    /**
     * Check if {@link User} has rights to edit a {@link ModelNode} in the specified {@link UserRoleManagement}.
     *
     * @param userRoleManagement {@link UserRoleManagement} to check in
     * @param user               checked {@link User}
     * @param modelNode          checked {@link ModelNode}
     * @return <i>true</i> if {@link User} has rights to edit {@link ModelNode}, <i>false</i> if opposite
     */
    boolean checkUserAccessToModelNode(UserRoleManagement userRoleManagement, User user, ModelNode modelNode);

    /**
     * Check if {@link User} is with <i>admin</i> rights in the specified {@link UserRoleManagement}.
     *
     * @param userRoleManagement {@link UserRoleManagement} to check in
     * @param user               checked {@link User}
     * @return <i>true</i> if {@link User} has an <i>admin</i> rights, <i>false</i> if opposite
     */
    boolean checkUserAdmin(UserRoleManagement userRoleManagement, User user);

    /**
     * Create default {@link UserRoleManagement}.
     *
     * @param userManagement {@link UserManagement} to base on
     * @return created instance of {@link UserRoleManagement}
     */
    UserRoleManagement createDefaultUserRoleManagement(UserManagement userManagement);

    /**
     * Build {@link UserRoleManagement} which contains <i>Admin</i> {@link Discipline} with full rights.
     *
     * @return UserRoleManagement instance
     */
    UserRoleManagement createUserRoleManagement();

    /**
     * Create {@link UserRoleManagement} based on {@link SystemModel} subsystems and
     * {@link UserManagement} disciplines.
     *
     * @param systemModel    {@link SystemModel} to base on
     * @param userManagement {@link UserManagement} to base on
     * @return created instance of {@link UserRoleManagement}
     */
    UserRoleManagement createUserRoleManagementWithSubsystemDisciplines(SystemModel systemModel, UserManagement userManagement);

    /**
     * Build a {@link Map} of current {@link UserRoleManagement} disciplines
     * (discipline's name as a key).
     *
     * @param userRoleManagement {@link UserRoleManagement} for build from
     * @return map with discipline names as keys and disciplines itself as values.
     */
    Map<String, Discipline> disciplineMap(UserRoleManagement userRoleManagement);

    /**
     * Retrieve an {@link UserRoleManagement} by id.
     *
     * @param userRoleManagementId must not be {@literal null}.
     * @return userRoleManagement
     */
    UserRoleManagement findUserRoleManagement(Long userRoleManagementId);

    /**
     * Retrieve an <i>admin</i> {@link Discipline} from {@link UserRoleManagement}.
     * If there is no one, then exception is thrown due to inconsistency.
     *
     * @param userRoleManagement {@link UserRoleManagement} for search in
     * @return <i>admin</i> {@link Discipline} founded.
     */
    Discipline obtainAdminDiscipline(UserRoleManagement userRoleManagement);

    /**
     * Retrieve a list of {@link Discipline}s related to the specified {@link ModelNode}.
     *
     * @param userRoleManagement {@link UserRoleManagement} for search in
     * @param modelNode          {@link ModelNode} to search for
     * @return list of {@link Discipline} related to the specified {@link ModelNode}
     */
    Discipline obtainDisciplineOfSubSystem(UserRoleManagement userRoleManagement, ModelNode modelNode);

    /**
     * Retrieve a list of {@link Discipline}s appointed to the {@link User}.
     *
     * @param userRoleManagement {@link UserRoleManagement} for search in
     * @param user               {@link User} to search for
     * @return list of {@link Discipline}s appointed to the {@link User}
     */
    List<Discipline> obtainDisciplinesOfUser(UserRoleManagement userRoleManagement, User user);

    /**
     * Retrieve a list of {@link SubSystemModel}s related to the specified {@link Discipline}.
     *
     * @param userRoleManagement {@link UserRoleManagement} for search in
     * @param discipline         {@link Discipline} to search for
     * @return list of {@link SubSystemModel} related to the specified {@link Discipline}
     */
    List<SubSystemModel> obtainSubSystemsOfDiscipline(UserRoleManagement userRoleManagement, Discipline discipline);

    /**
     * Retrieve a list of {@link User}s with particular {@link Discipline}.
     *
     * @param userRoleManagement {@link UserRoleManagement} for search in
     * @param discipline         {@link Discipline} to search for
     * @return list of {@link User}s with specified {@link Discipline}
     */
    List<User> obtainUsersOfDiscipline(UserRoleManagement userRoleManagement, Discipline discipline);

    /**
     * Remove {@link Discipline} from {@link UserRoleManagement}.
     *
     * @param userRoleManagement {@link UserRoleManagement} to remove from
     * @param discipline         discipline to remove
     */
    void removeDiscipline(UserRoleManagement userRoleManagement, Discipline discipline);

    /**
     * Saves an userRoleManagement.
     *
     * @param userRoleManagement userRoleManagement to save
     * @return the saved userRoleManagement
     */
    UserRoleManagement saveUserRoleManagement(UserRoleManagement userRoleManagement);
}
