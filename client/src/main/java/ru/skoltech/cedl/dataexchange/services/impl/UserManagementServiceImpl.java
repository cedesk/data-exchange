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

package ru.skoltech.cedl.dataexchange.services.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.entity.user.UserManagement;
import ru.skoltech.cedl.dataexchange.repository.revision.UserManagementRepository;
import ru.skoltech.cedl.dataexchange.services.UserManagementService;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.skoltech.cedl.dataexchange.repository.revision.UserManagementRepository.IDENTIFIER;

/**
 * Created by dknoll on 13/05/15.
 */
public class UserManagementServiceImpl implements UserManagementService {

    private static final Logger logger = Logger.getLogger(UserManagementServiceImpl.class);

    private final UserManagementRepository userManagementRepository;

    @Autowired
    public UserManagementServiceImpl(UserManagementRepository userManagementRepository) {
        this.userManagementRepository = userManagementRepository;
    }

    @Override
    public UserManagement createDefaultUserManagement() {
        UserManagement userManagement = new UserManagement();
        userManagement.setId(IDENTIFIER);

        User expert = new User(OBSERVER_USER_NAME, "Observer", "");
        User admin = new User(ADMIN_USER_NAME, "Team Lead", "");

        userManagement.getUsers().add(admin);
        userManagement.getUsers().add(expert);
        return userManagement;
    }

    @Override
    public UserManagement findUserManagement() {
        return userManagementRepository.findOne(UserManagementRepository.IDENTIFIER);
    }

    @Override
    public UserManagement saveUserManagement(UserManagement userManagement) {
        return userManagementRepository.saveAndFlush(userManagement);
    }

    @Override
    public Map<String, User> userMap(UserManagement userManagement) {
        return userManagement.getUsers().stream().collect(
                Collectors.toMap(User::getUserName, Function.identity()));
    }

    @Override
    public boolean checkUserName(UserManagement userManagement, String userName) {
        return userMap(userManagement).containsKey(userName);
    }

    @Override
    public User obtainUser(UserManagement userManagement, String userName) {
        User user = userMap(userManagement).get(userName);
        if (user == null) {
            logger.error("user not found: " + userName);
        }
        return user;
    }

}