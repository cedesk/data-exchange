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
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.entity.user.*;
import ru.skoltech.cedl.dataexchange.repository.revision.UserRoleManagementRepository;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.service.UserService;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of {@link UserRoleManagementService}.
 * <p>
 * Created by Nikolay Groshkov on 26-Jul-17.
 */
public class UserRoleManagementServiceImpl implements UserRoleManagementService {

    private static final Logger logger = Logger.getLogger(UserRoleManagementServiceImpl.class);
    private final UserRoleManagementRepository userRoleManagementRepository;
    private UserService userService;

    @Autowired
    public UserRoleManagementServiceImpl(UserRoleManagementRepository userRoleManagementRepository) {
        this.userRoleManagementRepository = userRoleManagementRepository;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private static ModelNode findOwningSubSystem(ModelNode modelNode) {
        if (modelNode.isRootNode()) {
            return modelNode;
        }
        ModelNode parent = modelNode.getParent();
        while (!parent.isRootNode()) {
            modelNode = parent;
            parent = parent.getParent();
        }
        return modelNode;
    }

    @Override
    public boolean addAdminDiscipline(UserRoleManagement userRoleManagement, User user) {
        Discipline adminDiscipline = this.obtainAdminDiscipline(userRoleManagement);
        return this.addUserDiscipline(userRoleManagement, user, adminDiscipline);
    }

    @Override
    public boolean addDisciplineSubsystem(UserRoleManagement userRoleManagement, Discipline discipline, SubSystemModel subSystem) {
        DisciplineSubSystem disciplineSubSystem = new DisciplineSubSystem(userRoleManagement, discipline, subSystem);
        boolean empty = this.obtainSubSystemsOfDiscipline(userRoleManagement, discipline).isEmpty();
        if (empty) {
            userRoleManagement.getDisciplineSubSystems().add(disciplineSubSystem);
        }
        return empty;
    }

    @Override
    public boolean addUserDiscipline(UserRoleManagement userRoleManagement, User user, Discipline discipline) {
        UserDiscipline userDiscipline = new UserDiscipline(userRoleManagement, user, discipline);
        boolean found = userRoleManagement.getUserDisciplines().contains(userDiscipline);
        if (!found) {
            userDiscipline.setUserRoleManagement(userRoleManagement);
            userRoleManagement.getUserDisciplines().add(userDiscipline);
        }
        return !found;
    }

    @Override
    public void addUserWithAdminRole(UserRoleManagement userRoleManagement, String userName) {
        User admin = new User(userName, userName + " (made admin)", "ad-hoc permissions for current user");
        admin = userService.saveUser(admin);
        boolean added = this.addAdminDiscipline(userRoleManagement, admin);
        if (!added) {
            logger.debug("Admin discipline has not been added.");
        }
    }

    @Override
    public boolean checkUserAccessToModelNode(UserRoleManagement userRoleManagement, User user, ModelNode modelNode) {
        // check username contained in user management
        if (user == null)
            return false;

        // check the users associated disciplines
        ModelNode subSystem = findOwningSubSystem(modelNode);
        Discipline discipline = this.obtainDisciplineOfSubSystem(userRoleManagement, subSystem);
        List<Discipline> disciplinesOfUser = this.obtainDisciplinesOfUser(userRoleManagement, user);
        for (Discipline userDiscipline : disciplinesOfUser) {
            if (userDiscipline.equals(discipline) || userDiscipline.isBuiltIn())
                return true;
        }
        return false;
    }

    @Override
    public boolean checkUserAdmin(UserRoleManagement userRoleManagement, User user) {
        if (userRoleManagement == null || user == null) {
            return false;
        }
        for (Discipline discipline : this.obtainDisciplinesOfUser(userRoleManagement, user)) {
            if (discipline.isBuiltIn()) return true;
        }
        return false;
    }

    @Override
    public UserRoleManagement createUserRoleManagement() {
        UserRoleManagement userRoleManagement = new UserRoleManagement();

        Discipline adminDiscipline = new Discipline("Admin", userRoleManagement, true);
        userRoleManagement.getDisciplines().add(adminDiscipline);
        return userRoleManagement;
    }

    @Override
    public UserRoleManagement createUserRoleManagementWithSubsystemDisciplines(SystemModel systemModel) {
        UserRoleManagement urm = this.createUserRoleManagement();

        // add a discipline for each subsystem
        for (ModelNode modelNode : systemModel.getSubNodes()) {
            Discipline discipline = new Discipline(modelNode.getName(), urm);
            urm.getDisciplines().add(discipline);
        }
        // add user disciplines
        User admin = userService.findUser(UserService.ADMIN_USER_NAME);
        if (admin != null) {
            this.addAdminDiscipline(urm, admin);
        }
        return urm;
    }

    @Override
    public Map<String, Discipline> disciplineMap(UserRoleManagement userRoleManagement) {
        return userRoleManagement.getDisciplines().stream().collect(
                Collectors.toMap(Discipline::getName, Function.identity()));
    }

    @Override
    public UserRoleManagement findUserRoleManagement(Long userRoleManagementId) {
        return userRoleManagementRepository.findOne(userRoleManagementId);
    }

    @Override
    public Discipline obtainAdminDiscipline(UserRoleManagement userRoleManagement) {
        List<Discipline> adminDisciplines =
                userRoleManagement.getDisciplines().stream()
                        .filter(Discipline::isBuiltIn).collect(Collectors.toList());

        if (adminDisciplines.isEmpty()) {
            throw new RuntimeException("inconsistent UserManagement!");
        }
        return adminDisciplines.get(0);
    }

    @Override
    public Discipline obtainDisciplineOfSubSystem(UserRoleManagement userRoleManagement, ModelNode modelNode) {
        if (modelNode.isRootNode()) {
            return this.obtainAdminDiscipline(userRoleManagement);
        }
        Optional<DisciplineSubSystem> subSystemOptional = userRoleManagement.getDisciplineSubSystems().stream()
                .filter(disciplineSubSystem -> disciplineSubSystem.getSubSystem().getUuid().equals(modelNode.getUuid()))
                .findAny();

        if (subSystemOptional.isPresent()) {
            return subSystemOptional.get().getDiscipline();
        } else {
            logger.debug("no discipline found for subsystem '" + modelNode.getName() + "'");
            return null;
        }
    }

    @Override
    public List<Discipline> obtainDisciplinesOfUser(UserRoleManagement userRoleManagement, User user) {
        return userRoleManagement.getUserDisciplines().stream()
                .filter(userDiscipline -> userDiscipline.getUser().equals(user))
                .map(UserDiscipline::getDiscipline).distinct()
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public List<SubSystemModel> obtainSubSystemsOfDiscipline(UserRoleManagement userRoleManagement, Discipline discipline) {
        return userRoleManagement.getDisciplineSubSystems().stream()
                .filter(disciplineSubSystem -> disciplineSubSystem.getDiscipline().equals(discipline))
                .map(DisciplineSubSystem::getSubSystem).distinct()
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public List<User> obtainUsersOfDiscipline(UserRoleManagement userRoleManagement, Discipline discipline) {
        return userRoleManagement.getUserDisciplines().stream()
                .filter(userDiscipline -> userDiscipline.getDiscipline().equals(discipline))
                .map(UserDiscipline::getUser).distinct()
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public void removeDiscipline(UserRoleManagement userRoleManagement, Discipline discipline) {
        userRoleManagement.getUserDisciplines().removeIf(userDiscipline -> userDiscipline.getDiscipline().equals(discipline));
        userRoleManagement.getDisciplineSubSystems().removeIf(disciplineSubSystem -> disciplineSubSystem.getDiscipline().equals(discipline));
        userRoleManagement.getDisciplines().remove(discipline);
    }

    @Override
    public UserRoleManagement saveUserRoleManagement(UserRoleManagement userRoleManagement) {
        return userRoleManagementRepository.save(userRoleManagement);
    }

}