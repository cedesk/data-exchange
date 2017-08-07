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

package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.services.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for user management.
 *
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

    private FXMLLoaderFactory fxmlLoaderFactory;
    private Project project;
    private UserRoleManagementService userRoleManagementService;

    public void setFxmlLoaderFactory(FXMLLoaderFactory fxmlLoaderFactory) {
        this.fxmlLoaderFactory = fxmlLoaderFactory;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setUserRoleManagementService(UserRoleManagementService userRoleManagementService) {
        this.userRoleManagementService = userRoleManagementService;
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

    private void updateUsers() {
        if (project.getUserManagement() != null) {
            List<User> allUsers = project.getUserManagement().getUsers();
            userTable.getItems().clear();
            userTable.getItems().addAll(allUsers);
            userTable.getItems().sort(Comparator.naturalOrder());
        }
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
                StatusLogger.getInstance().log("added user: " + user.getUserName());
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

    public void reloadUsers(ActionEvent actionEvent) {
        boolean success = project.loadUserManagement();
        updateUsers();
        if (!success) {
            StatusLogger.getInstance().log("Error loading user list!", true);
        }
    }

    public void saveUsers(ActionEvent actionEvent) {
        boolean success = project.storeUserManagement();
        if (!success) {
            StatusLogger.getInstance().log("Error saving user list!", true);
        }
    }

    public void openUserEditingView(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = fxmlLoaderFactory.createFXMLLoader(Views.USER_EDITING_WINDOW);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("User details");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(userTable.getScene().getWindow());

            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            UserEditingController controller = loader.getController();
            controller.setUserModel(selectedUser);

            stage.showAndWait();
            updateUsers();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void updateView() {
        updateUsers();
    }

}
