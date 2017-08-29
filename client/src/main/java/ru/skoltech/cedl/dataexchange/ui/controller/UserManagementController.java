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

package ru.skoltech.cedl.dataexchange.ui.controller;

import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for user management.
 * <p>
 * Created by d.knoll on 10.06.2015.
 */
public class UserManagementController implements Initializable {

    private static final Logger logger = Logger.getLogger(UserManagementController.class);

    @FXML
    private TableView<User> userTable;

    @FXML
    private Button addUserButton;

    @FXML
    private Button editUserButton;

    @FXML
    private Button deleteUserButton;

    private Project project;
    private GuiService guiService;
    private UserRoleManagementService userRoleManagementService;
    private StatusLogger statusLogger;

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setUserRoleManagementService(UserRoleManagementService userRoleManagementService) {
        this.userRoleManagementService = userRoleManagementService;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    public void addUser(ActionEvent actionEvent) {
        Optional<String> userNameChoice = Dialogues.inputUserName();
        if (userNameChoice.isPresent()) {
            String userName = userNameChoice.get();
            if (!Identifiers.validateUserName(userName)) {
                Dialogues.showError("Invalid name", Identifiers.getUserNameValidationDescription());
                return;
            }
            if (userRoleManagementService.disciplineMap(project.getUserRoleManagement()).containsKey(userName)) {
                Dialogues.showError("Duplicate user name", "There is already a user named like that!");
            } else {
                User user = new User();
                user.setUserName(userName);
                statusLogger.info("added user: " + user.getUserName());
                project.getUserManagement().getUsers().add(user);
            }
        }
        updateUsers();
    }

    public void deleteUser(ActionEvent actionEvent) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        project.getUserManagement().getUsers().remove(selectedUser);
        updateUsers();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        BooleanBinding noSelectionOnUserTable = userTable.getSelectionModel().selectedItemProperty().isNull();
        // USERS
        editUserButton.disableProperty().bind(noSelectionOnUserTable);
        deleteUserButton.disableProperty().bind(noSelectionOnUserTable);
        userTable.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                UserManagementController.this.openUserEditingView(null);
            }
        });
        updateView();
    }

    public void openUserEditingView(ActionEvent actionEvent) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        Window ownerWindow = userTable.getScene().getWindow();
        ViewBuilder userDetailsViewBuilder = guiService.createViewBuilder("User details", Views.USER_EDITING_VIEW);
        userDetailsViewBuilder.ownerWindow(ownerWindow);
        userDetailsViewBuilder.modality(Modality.APPLICATION_MODAL);
        userDetailsViewBuilder.showAndWait(selectedUser);
        updateUsers();
    }

    public void reloadUsers(ActionEvent actionEvent) {
        boolean success = project.loadUserManagement();
        updateUsers();
        if (!success) {
            statusLogger.error("Error loading user list!");
        }
    }

    public void saveUsers(ActionEvent actionEvent) {
        boolean success = project.storeUserManagement();
        if (!success) {
            statusLogger.error("Error saving user list!");
        }
    }

    private void updateUsers() {
        if (project.getUserManagement() != null) {
            List<User> allUsers = project.getUserManagement().getUsers();
            userTable.getItems().clear();
            userTable.getItems().addAll(allUsers);
            userTable.getItems().sort(Comparator.naturalOrder());
        }
    }

    private void updateView() {
        updateUsers();
    }

}
