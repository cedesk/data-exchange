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

import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.service.UserService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for user management.
 * <p>
 * Created by d.knoll on 10.06.2015.
 */
public class UserManagementController implements Initializable, Displayable {

    private static final Logger logger = Logger.getLogger(UserManagementController.class);

    @FXML
    public TextField filterTextField;
    @FXML
    private TableView<User> userTable;
    @FXML
    private Button editUserButton;
    @FXML
    private Button deleteUserButton;

    private Project project;
    private UserService userService;
    private GuiService guiService;
    private UserRoleManagementService userRoleManagementService;
    private StatusLogger statusLogger;

    private Stage ownerStage;

    private ListProperty<User> usersProperty = new SimpleListProperty<>(FXCollections.emptyObservableList());

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    public void setUserRoleManagementService(UserRoleManagementService userRoleManagementService) {
        this.userRoleManagementService = userRoleManagementService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userTable.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            List<User> filteredUsers = usersProperty.stream()
                    .filter(user -> (user.getUserName().toLowerCase().contains(filterTextField.getText().toLowerCase())
                            || (user.getFullName() != null &&
                            user.getFullName().toLowerCase().contains(filterTextField.getText().toLowerCase()))))
                    .collect(Collectors.toList());
            return FXCollections.observableList(filteredUsers);
        }, filterTextField.textProperty(), usersProperty));

        this.reloadUsers();

        userTable.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                UserManagementController.this.editUser();
            }
        });

        editUserButton.disableProperty().bind(userTable.getSelectionModel().selectedItemProperty().isNull());
        deleteUserButton.disableProperty().bind(userTable.getSelectionModel().selectedItemProperty().isNull());
    }

    public void addUser() {
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
                logger.debug("Add user: " + user.getUserName());
                statusLogger.info("Add user: " + user.getUserName());
                userService.saveUser(user);
            }
        }
        this.reloadUsers();
    }

    public void editUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        Window ownerWindow = userTable.getScene().getWindow();
        ViewBuilder userDetailsViewBuilder = guiService.createViewBuilder("User details", Views.USER_EDITING_VIEW);
        userDetailsViewBuilder.ownerWindow(ownerWindow);
        userDetailsViewBuilder.modality(Modality.APPLICATION_MODAL);
        userDetailsViewBuilder.showAndWait(selectedUser);
        this.reloadUsers();
        userTable.refresh();
    }

    public void deleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        logger.debug("Remove user: " + selectedUser.getUserName());
        userService.deleteUser(selectedUser);
        this.reloadUsers();
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    @FXML
    public void close() {
        ownerStage.close();
    }

    private void reloadUsers() {
        List<User> users = userService.findAllUsers();
        users.sort(Comparator.naturalOrder());
        this.usersProperty.set(FXCollections.observableList(users));
    }
}
