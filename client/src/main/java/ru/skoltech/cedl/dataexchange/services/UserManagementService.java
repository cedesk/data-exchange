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

import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.entity.user.UserManagement;

import java.util.Map;

/**
 * Operations with {@link UserManagement}.
 *
 * Created by Nikolay Groshkov on 06-Jul-17.
 */
public interface UserManagementService {

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
     * Retrieve an {@link UserManagement}
     *
     * @return userManagement
     */
    UserManagement findUserManagement();

    /**
     * Saves an userManagement.
     *
     * @param userManagement userManagement to save
     * @return the saved userManagement
     */
    UserManagement saveUserManagement(UserManagement userManagement);

    /**
     * Build a {@link Map} of current {@link User}s (user name as a key).
     *
     * @param userManagement {@link UserManagement} to build from
     * @return map with user names as keys and users itself as values.
     */
    Map<String, User> userMap(UserManagement userManagement);

    /**
     * Check if {@link UserManagement} contains {@link User} with specified name.
     *
     * @param userManagement {@link UserManagement} to check from
     * @param userName checked user name
     * @return <i>true</i> if {@link User} exists, <i>false</i> if opposite
     */
    boolean checkUserName(UserManagement userManagement, String userName);

    /**
     * Retrieve from {@link UserManagement} an {@link User} with specified name.
     *
     * @param userManagement {@link UserManagement} to search from
     * @param userName to search for
     * @return instance of {@link User} if found, <i>null</i> if opposite
     */
    User obtainUser(UserManagement userManagement, String userName);
}
