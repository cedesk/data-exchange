package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.users.UserRoleUtil;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by d.knoll on 10.06.2015.
 */
public class UserRoleManagementController implements Initializable {

    @FXML
    public TableView disciplinesTable;

    @FXML
    public TableView subsystemsTable;

    @FXML
    public TableView userTable;

    @FXML
    public Button addDisciplineButton;

    @FXML
    public Button deleteDisciplineButton;

    @FXML
    public Button addSubsystemButton;

    @FXML
    public Button deleteSubsystemButton;

    @FXML
    public Button addUserButton;

    @FXML
    public Button deleteUserButton;

    @FXML
    public TableColumn disciplineNameColumn;

    @FXML
    public TableColumn disciplineDescriptionColumn;

    private Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // DISCIPLINE TABLE
        disciplinesTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (newValue != null) {
                    Discipline discipline = (Discipline) newValue;
                    updateSubsystemTable(discipline);
                    updateUserTable(discipline);
                    subsystemsTable.setDisable(false);
                    userTable.setDisable(false);
                } else {
                    subsystemsTable.setDisable(true);
                    userTable.setDisable(true);
                }
            }
        });
        Callback<TableColumn<Object, String>, TableCell<Object, String>> tableCellCallback = TextFieldTableCell.forTableColumn();
        disciplineNameColumn.setCellFactory(tableCellCallback);
        disciplineDescriptionColumn.setCellFactory(tableCellCallback);
    }

    public void updateView() {
        updateDisciplineTable();
    }

    private void updateDisciplineTable() {
        if (project.getUserRoleManagement() != null) {
            List<Discipline> disciplines = project.getUserRoleManagement().getDisciplines();
            ObservableList<Discipline> disciplineList = FXCollections.observableList(disciplines);
            disciplinesTable.setItems(disciplineList);
            boolean editable = UserRoleUtil.isAdmin(project.getUser());
            disciplinesTable.setEditable(editable);
        }
    }

    private void updateSubsystemTable(Discipline discipline) {
        if (project.getSystemModel() != null) {
            List<SubSystemModel> modelNodes = project.getSystemModel().getSubNodes();
            ObservableList<SubSystemModel> nodesList = FXCollections.observableList(modelNodes);
            subsystemsTable.setItems(nodesList);
        }
    }

    private void updateUserTable(Discipline discipline) {
        if (project.getUserManagement() != null) {
            List<User> users = project.getUserManagement().getUsers();
            ObservableList<User> userList = FXCollections.observableList(users);
            userTable.setItems(userList);
        }
    }

    public void addDiscipline(ActionEvent actionEvent) {
        Optional<String> disciplineNameChoice = Dialogues.inputDisciplineName("new-discipline");
        if (disciplineNameChoice.isPresent()) {
            String disciplineName = disciplineNameChoice.get();

            if (!Identifiers.validateNodeName(disciplineName)) {
                Dialogues.showError("Invalid name", Identifiers.getNameValidationDescription());
                return;
            }
            if (project.getUserRoleManagement().getDisciplineMap().containsKey(disciplineName)) {
                Dialogues.showError("Duplicate discipline name", "There is already a discipline named like that!");
            } else {
                Discipline discipline = new Discipline(disciplineName, project.getUserRoleManagement());
                project.getUserRoleManagement().getDisciplines().add(discipline);
                project.markStudyModified();
                StatusLogger.getInstance().log("added discipline: " + discipline.getName());
            }
        }
        updateDisciplineTable();
    }

    public void deleteDiscipline(ActionEvent actionEvent) {
        Discipline selectedDiscipline = getSelectedDiscipline();
        if (selectedDiscipline != null)
            throw new AssertionError("no discipline in table view");
        project.getUserRoleManagement().getDisciplines().remove(selectedDiscipline);
        project.markStudyModified();
        StatusLogger.getInstance().log("removed discipline: " + selectedDiscipline.getName());
        updateDisciplineTable();
    }

    public void addSubsystem(ActionEvent actionEvent) {
    }

    public void deleteSubsystem(ActionEvent actionEvent) {
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
                // TODO: use factory
                User user = new User();
                user.setUserName(userName);
                StatusLogger.getInstance().log("added user: " + user.getUserName());
                project.getUserManagement().getUsers().add(user);
            }
        }
        updateUserTable(getSelectedDiscipline());
    }

    public void deleteUser(ActionEvent actionEvent) {
        project.getUserManagement().getUsers().remove(getSelectedUser());
        updateUserTable(getSelectedDiscipline());
    }

    public Discipline getSelectedDiscipline() {
        return (Discipline) disciplinesTable.getSelectionModel().getSelectedItem();
    }

    public ModelNode getSelectedSubsystem() {
        return (ModelNode) subsystemsTable.getSelectionModel().getSelectedItem();
    }

    public User getSelectedUser() {
        return (User) userTable.getSelectionModel().getSelectedItem();
    }
}
