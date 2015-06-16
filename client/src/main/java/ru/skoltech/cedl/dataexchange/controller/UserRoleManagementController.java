package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserDiscipline;

import java.net.URL;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by d.knoll on 10.06.2015.
 */
public class UserRoleManagementController implements Initializable {

    @FXML
    public TableView disciplinesTable;

    @FXML
    public TableView userTable;

    @FXML
    public Button addDisciplineButton;

    @FXML
    public Button deleteDisciplineButton;

    @FXML
    public ListView subsystemsAssignedList;

    @FXML
    public ListView subsystemsAvailableList;

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

    @FXML
    public Pane subsystemsPane;

    @FXML
    public ListView userRolesAssignedList;

    @FXML
    public Button addUserRoleButton;

    @FXML
    public Button deleteUserRoleButton;

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
                    updateSubsystems(discipline);
                    updateUsers(discipline);
                }
            }
        });
        Callback<TableColumn<Object, String>, TableCell<Object, String>> tableCellCallback = TextFieldTableCell.forTableColumn();
        disciplineNameColumn.setCellFactory(tableCellCallback);
        disciplineDescriptionColumn.setCellFactory(tableCellCallback);

        // SUB-SYSTEMS
        subsystemsPane.disableProperty().bind(disciplinesTable.getSelectionModel().selectedItemProperty().isNull());

        subsystemsAvailableList.setCellFactory(new SubsystemsViewCellFactory());
        subsystemsAssignedList.setCellFactory(new SubsystemsViewCellFactory());
        addSubsystemButton.disableProperty().bind(subsystemsAvailableList.getSelectionModel().selectedItemProperty().isNull());
        deleteSubsystemButton.disableProperty().bind(subsystemsAssignedList.getSelectionModel().selectedItemProperty().isNull());

        // USER-ROLES
        userRolesAssignedList.disableProperty().bind(disciplinesTable.getSelectionModel().selectedItemProperty().isNull());
        userRolesAssignedList.setCellFactory(new UserDisciplineViewCellFactory());
        addUserRoleButton.disableProperty().bind(userTable.getSelectionModel().selectedItemProperty().isNull());
        deleteUserRoleButton.disableProperty().bind(userRolesAssignedList.getSelectionModel().selectedItemProperty().isNull());

        // USERS
        deleteUserButton.disableProperty().bind(userTable.getSelectionModel().selectedItemProperty().isNull());
    }

    public void updateView() {
        updateDisciplineTable();
    }

    private void updateDisciplineTable() {
        if (project.getUserRoleManagement() != null) {
            List<Discipline> disciplines = project.getUserRoleManagement().getDisciplines();
            ObservableList<Discipline> disciplineList = FXCollections.observableList(disciplines);
            disciplinesTable.setItems(disciplineList);
            boolean editable = project.getUserRoleManagement().isAdmin(project.getUser());
            disciplinesTable.setEditable(editable);
        }
    }

    private void updateSubsystems(Discipline discipline) {
        if (project.getSystemModel() != null) {
            //TODO: getAssignedSubsystems
            ObservableList<Object> emptyList = FXCollections.observableArrayList(new Object[]{});
            subsystemsAssignedList.setItems(emptyList);
            //TODO: change to getAllSubsystems - assignedSubsystems
            List<SubSystemModel> modelNodes = project.getSystemModel().getSubNodes();
            List<SubSystemModel> subsystemsList = new ArrayList<>(modelNodes);
            ObservableList<SubSystemModel> nodesList = FXCollections.observableList(subsystemsList);
            subsystemsAvailableList.setItems(nodesList);
        }
    }

    private void updateUsers(Discipline discipline) {
        if (project.getUserManagement() != null) {
            // all Users
            List<User> allUsers = project.getUserManagement().getUsers();
            ObservableList<User> allUserList = FXCollections.observableList(allUsers);
            allUserList.sort(Comparator.<User>naturalOrder());
            userTable.setItems(allUserList);

            // assigned Users
            List<UserDiscipline> userDisciplineList = project.getUserRoleManagement().getUserDisciplines();
            ObservableList allUserDisciplines = FXCollections.observableArrayList(userDisciplineList);
            ObservableList assignedUsersList = new FilteredList<UserDiscipline>(allUserDisciplines, new DisciplineFilter(discipline));
            userRolesAssignedList.setItems(assignedUsersList);
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
        ModelNode selectedItem = (ModelNode) subsystemsAvailableList.getSelectionModel().getSelectedItem();
        subsystemsAvailableList.getItems().remove(selectedItem);
        subsystemsAssignedList.getItems().add(selectedItem);
        //TODO: record in DB-backed data model
    }

    public void deleteSubsystem(ActionEvent actionEvent) {
        ModelNode selectedItem = (ModelNode) subsystemsAssignedList.getSelectionModel().getSelectedItem();
        subsystemsAssignedList.getItems().remove(selectedItem);
        subsystemsAvailableList.getItems().add(selectedItem);
        //TODO: record in DB-backed data model
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
        updateUsers(getSelectedDiscipline());
    }

    public void deleteUser(ActionEvent actionEvent) {
        project.getUserManagement().getUsers().remove(getSelectedUser());
        updateUsers(getSelectedDiscipline());
    }

    public Discipline getSelectedDiscipline() {
        return (Discipline) disciplinesTable.getSelectionModel().getSelectedItem();
    }

    public User getSelectedUser() {
        return (User) userTable.getSelectionModel().getSelectedItem();
    }

    public void addUserRole(ActionEvent actionEvent) {
        project.getUserRoleManagement().addUserDiscipline(getSelectedUser(), getSelectedDiscipline());
        updateUsers(getSelectedDiscipline());
        // TODO: record in DB-backed data model
    }

    public void deleteUserRole(ActionEvent actionEvent) {
        UserDiscipline selectedUserDiscipline = (UserDiscipline) userRolesAssignedList.getSelectionModel().getSelectedItem();
        userRolesAssignedList.getItems().remove(selectedUserDiscipline);
        updateUsers(getSelectedDiscipline());
        // TODO: record in DB-backed data model
    }

    private class SubsystemsViewCellFactory implements Callback<ListView<SubSystemModel>, ListCell<SubSystemModel>> {
        @Override
        public ListCell<SubSystemModel> call(ListView<SubSystemModel> p) {
            ListCell<SubSystemModel> cell = new ListCell<SubSystemModel>() {
                @Override
                protected void updateItem(SubSystemModel model, boolean blank) {
                    super.updateItem(model, blank);
                    if (model != null && !blank) {
                        setText(model.getName());
                    }
                }
            };
            return cell;
        }
    }

    private class UserDisciplineViewCellFactory implements Callback<ListView<UserDiscipline>, ListCell<UserDiscipline>> {
        @Override
        public ListCell<UserDiscipline> call(ListView<UserDiscipline> p) {
            ListCell<UserDiscipline> cell = new ListCell<UserDiscipline>() {
                @Override
                protected void updateItem(UserDiscipline userDiscipline, boolean blank) {
                    super.updateItem(userDiscipline, blank);
                    if (userDiscipline != null && !blank) {
                        User user = userDiscipline.getUser();
                        setText(user.getUserName() + " (" + user.getFullName() + ")");
                    }
                }
            };
            return cell;
        }
    }

    private class DisciplineFilter implements Predicate<UserDiscipline> {
        private Discipline filterDiscipline;

        public DisciplineFilter(Discipline discipline) {
            this.filterDiscipline = discipline;
        }

        @Override
        public boolean test(UserDiscipline userDiscipline) {
            return userDiscipline.getDiscipline() == filterDiscipline;
        }
    }
}
