package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
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

    public void setProject(Project project) {
        this.project = project;
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
            if (project.getUserRoleManagement().getDisciplineMap().containsKey(userName)) {
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
        project.getUserManagement().getUsers().remove(getSelectedUser());
        updateUsers();
    }

    public User getSelectedUser() {
        return (User) userTable.getSelectionModel().getSelectedItem();
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
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Views.USER_EDITING_VIEW);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("User details");
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(getAppWindow());

            UserEditingController controller = loader.getController();
            controller.setUserModel(getSelectedUser());
            stage.showAndWait();
            updateUsers();
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public Window getAppWindow() {
        return userTable.getScene().getWindow();
    }

    public void updateView() {
        updateUsers();
    }
}