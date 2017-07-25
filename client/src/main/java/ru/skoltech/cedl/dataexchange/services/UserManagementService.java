/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.services;

import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserManagement;
import ru.skoltech.cedl.dataexchange.users.model.UserRoleManagement;

/**
 * Operations with {@link UserManagement}.
 *
 * Created by Nikolay Groshkov on 06-Jul-17.
 */
public interface UserManagementService {

    /**
     * Default {@link UserManagement} id in the database
     */
    Long IDENTIFIER = 1L;

    /**
     * {@link User} name of administrator.
     */
    String ADMIN_USER_NAME = "admin";

    /**
     * {@link User} name of observer.
     */
    String OBSERVER_USER_NAME = "observer";

    /**
     * Create default {@link UserManagement}.
     *
     * @return default user management.
     */
    UserManagement createDefaultUserManagement();

    /**
     * Create {@link UserRoleManagement} based on {@link SystemModel} subsystems and
     * {@link UserManagement} disciplines.
     *
     * @param systemModel
     * @param userManagement
     * @return user role management
     */
    UserRoleManagement createUserRoleManagementWithSubsystemDisciplines(SystemModel systemModel, UserManagement userManagement);

    /**
     * Create default {@link UserRoleManagement}.
     *
     * @param userManagement
     * @return user role management
     */
    UserRoleManagement createDefaultUserRoleManagement(UserManagement userManagement);

    /**
     * Add user with specified name and set an administrator role to him.
     *
     * @param userRoleManagement
     * @param userManagement
     * @param userName
     */
    void addUserWithAdminRole(UserRoleManagement userRoleManagement, UserManagement userManagement, String userName);
}
