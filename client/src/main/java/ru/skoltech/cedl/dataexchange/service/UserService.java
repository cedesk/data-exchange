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

import ru.skoltech.cedl.dataexchange.entity.user.User;

import java.util.List;

/**
 * Operations with {@link User}.
 * <p>
 * Created by Nikolay Groshkov on 06-Jul-17.
 */
public interface UserService {

    /**
     * {@link User} name of administrator.
     */
    String ADMIN_USER_NAME = "admin";

    /**
     * {@link User} name of observer.
     */
    String OBSERVER_USER_NAME = "observer";

    /**
     * Retrieve a list of all available users.
     *
     * @return list of stored users
     */
    List<User> findAllUsers();

    /**
     * Retrieve an {@link User} with specified name.
     *
     * @param userName       to search for
     * @return instance of {@link User} if found, <i>null</i> if opposite
     */
    User findUser(String userName);

    /**
     * Retrieve an {@link User} with administrator rights.
     *
     * @return instance of {@link User} if found, <i>null</i> if opposite
     */
    User findAdminUser();

    /**
     * Check if {@link User} with specified name exists.
     *
     * @param userName       checked user name
     * @return <i>true</i> if {@link User} exists, <i>false</i> if opposite
     */
    boolean checkUser(String userName);

    /**
     * Saves an user instance.
     *
     * @param user user to save
     * @return the saved user
     */
    User saveUser(User user);

    /**
     * Delete user from database.
     *
     * @param user a user instance to delete
     */
    void deleteUser(User user);

    /**
     * Create default users.
     */
    void createDefaultUsers();
}
