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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.repository.revision.UserRepository;
import ru.skoltech.cedl.dataexchange.service.UserService;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link UserService}.
 * <p>
 * Created by dknoll on 13/05/15.
 */
public class UserServiceImpl implements UserService {

    private static final Logger logger = Logger.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> findAllUsers() {
        return this.userRepository.findAll();
    }

    @Override
    public User findUser(String userName) {
        User user = this.userRepository.findByUserName(userName);
        if (user == null) {
            logger.error("user not found: " + userName);
        }
        return user;
    }

    @Override
    public User findAdminUser() {
        return this.findUser(ADMIN_USER_NAME);
    }

    @Override
    public boolean checkUser(String userName) {
        return userRepository.existsByName(userName);
    }

    public User createUser(String userName, String fullName) {
        Objects.requireNonNull(userName);

        User user = new User(userName, fullName);
        return userRepository.saveAndFlush(user);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.saveAndFlush(user);
    }

    @Override
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    @Override
    public void createDefaultUsers() {
        User admin = new User(ADMIN_USER_NAME, "Team Lead", "");
        User expert = new User(OBSERVER_USER_NAME, "Observer", "");

        if (!this.checkUser(ADMIN_USER_NAME)) {
            userRepository.saveAndFlush(admin);
        }
        if (!this.checkUser(OBSERVER_USER_NAME)) {
            userRepository.saveAndFlush(expert);
        }
    }
}