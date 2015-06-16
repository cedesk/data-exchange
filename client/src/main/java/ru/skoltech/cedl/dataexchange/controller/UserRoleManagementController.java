package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
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
import ru.skoltech.cedl.dataexchange.structure.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.DisciplineSubSystem;
import ru.skoltech.cedl.dataexchange.users.model.User;
import ru.skoltech.cedl.dataexchange.users.model.UserDiscipline;

import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    @FXML
    public TableColumn subsystemCountColumn;

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
        disciplineNameColumn.setCellFactory(new DisciplineNameCellFactory());
        Callback<TableColumn<Object, String>, TableCell<Object, String>> tableCellCallback = TextFieldTableCell.forTableColumn();
        disciplineDescriptionColumn.setCellFactory(tableCellCallback);
        subsystemCountColumn.setCellFactory(tableCellCallback);
        subsystemCountColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Discipline, String>, ObservableValue>() {
            @Override
            public ObservableValue call(TableColumn.CellDataFeatures<Discipline, String> param) {
                Discipline discipline = param.getValue();
                if (discipline != null) {
                    long subSystemsCount = project.getUserRoleManagement().getSubSystemsOfDiscipline(discipline);
                    return new SimpleStringProperty(String.valueOf(subSystemsCount));
                } else {
                    return new SimpleStringProperty("");
                }
            }
        });

        // SUB-SYSTEMS
        BooleanBinding noSelectionOnDisciplinesTable = disciplinesTable.getSelectionModel().selectedItemProperty().isNull();
        subsystemsPane.disableProperty().bind(noSelectionOnDisciplinesTable);

        subsystemsAvailableList.setCellFactory(new SubsystemsViewCellFactory());
        subsystemsAssignedList.setCellFactory(new DisciplineSubSystemViewCellFactory());
        BooleanBinding noSelectionOnAvailableSubsystems = subsystemsAvailableList.getSelectionModel().selectedItemProperty().isNull();
        addSubsystemButton.disableProperty().bind(noSelectionOnAvailableSubsystems);
        BooleanBinding noSelectionOnAssignedSubsystems = subsystemsAssignedList.getSelectionModel().selectedItemProperty().isNull();
        deleteSubsystemButton.disableProperty().bind(noSelectionOnAssignedSubsystems);

        // USER-ROLES
        userRolesAssignedList.disableProperty().bind(noSelectionOnDisciplinesTable);
        userRolesAssignedList.setCellFactory(new UserDisciplineViewCellFactory());
        BooleanBinding noSelectionOnUserTable = userTable.getSelectionModel().selectedItemProperty().isNull();
        addUserRoleButton.disableProperty().bind(Bindings.and(noSelectionOnUserTable, noSelectionOnDisciplinesTable));
        BooleanBinding noSelectionOnAssignedUsers = userRolesAssignedList.getSelectionModel().selectedItemProperty().isNull();
        deleteUserRoleButton.disableProperty().bind(noSelectionOnAssignedUsers);

        // USERS
        deleteUserButton.disableProperty().bind(noSelectionOnUserTable);
    }

    public void updateView() {
        updateDisciplineTable();
        updateUsers(null);
    }

    private void updateDisciplineTable() {
        if (project.getUserRoleManagement() != null) {
            List<Discipline> disciplines = project.getUserRoleManagement().getDisciplines();
            ObservableList<Discipline> disciplineList = FXCollections.observableList(disciplines);
            disciplineList.sort(Comparator.<Discipline>naturalOrder());
            disciplinesTable.setItems(disciplineList);
            boolean editable = project.getUserRoleManagement().isAdmin(project.getUser());
            disciplinesTable.setEditable(editable);
        }
    }

    private void updateSubsystems(Discipline discipline) {
        if (project.getSystemModel() != null) {
            if (discipline != null) {
                // all discipline-subsystem assignments
                List<DisciplineSubSystem> disciplineSubSystemList = project.getUserRoleManagement().getDisciplineSubSystems();
                ObservableList allDisciplineSubsystems = FXCollections.observableArrayList(disciplineSubSystemList);
                ObservableList assignedSubsystemsList = new FilteredList<DisciplineSubSystem>(allDisciplineSubsystems, new DisciplineSubsystemFilter(discipline));
                subsystemsAssignedList.setItems(assignedSubsystemsList);

                // get assigned subsystems
                LinkedList<SubSystemModel> assignedSubsystems = disciplineSubSystemList.stream()
                        .map(DisciplineSubSystem::getSubSystem).distinct()
                        .collect(Collectors.toCollection(() -> new LinkedList<>()));
                // get all subsystems
                List<SubSystemModel> subsystemsList = new ArrayList<>(project.getSystemModel().getSubNodes());
                // retain only un-assigned subsystems
                subsystemsList.removeAll(assignedSubsystems);
                ObservableList<SubSystemModel> nodesList = FXCollections.observableList(subsystemsList);
                nodesList.sort(Comparator.<SubSystemModel>naturalOrder());
                subsystemsAvailableList.setItems(nodesList);
            }
        }
    }

    private void updateUsers(Discipline discipline) {
        if (project.getUserManagement() != null) {
            if (project.getUserManagement() != null) {
                // all Users
                List<User> allUsers = project.getUserManagement().getUsers();
                ObservableList<User> allUserList = FXCollections.observableList(allUsers);
                allUserList.sort(Comparator.<User>naturalOrder());
                userTable.setItems(allUserList);

                if (discipline != null) {
                    // assigned Users
                    List<UserDiscipline> userDisciplineList = project.getUserRoleManagement().getUserDisciplines();
                    ObservableList allUserDisciplines = FXCollections.observableArrayList(userDisciplineList);
                    ObservableList assignedUsersList = new FilteredList<UserDiscipline>(allUserDisciplines, new UserDisciplineFilter(discipline));
                    userRolesAssignedList.setItems(assignedUsersList);
                }
            }
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
        if (selectedDiscipline == null)
            throw new AssertionError("no discipline in table view");
        project.getUserRoleManagement().getDisciplines().remove(selectedDiscipline);
        project.markStudyModified();
        StatusLogger.getInstance().log("removed discipline: " + selectedDiscipline.getName());
        updateDisciplineTable();
    }

    public void addDisciplineSubsystem(ActionEvent actionEvent) {
        SubSystemModel subsystem = (SubSystemModel) subsystemsAvailableList.getSelectionModel().getSelectedItem();
        Discipline discipline = getSelectedDiscipline();
        project.getUserRoleManagement().addDisciplineSubsystem(discipline, subsystem);
        updateSubsystems(discipline);
        project.markStudyModified();
    }

    public void deleteDisciplineSubsystem(ActionEvent actionEvent) {
        DisciplineSubSystem disciplineSubsystem = (DisciplineSubSystem) subsystemsAssignedList.getSelectionModel().getSelectedItem();
        project.getUserRoleManagement().getDisciplineSubSystems().remove(disciplineSubsystem);
        updateSubsystems(getSelectedDiscipline());
        project.markStudyModified();
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
        User user = getSelectedUser();
        Discipline discipline = getSelectedDiscipline();
        if (user == null || discipline == null)
            throw new AssertionError("user or discipline must not be null");
        boolean duplicate = project.getUserRoleManagement().addUserDiscipline(user, discipline);
        if (duplicate) {
            StatusLogger.getInstance().log("user '" + user.getUserName() + "' can not be added twice to a discipline '" + discipline.getName() + "'");
        }
        updateUsers(discipline);
        project.markStudyModified();
    }

    public void deleteUserRole(ActionEvent actionEvent) {
        UserDiscipline selectedUserDiscipline = (UserDiscipline) userRolesAssignedList.getSelectionModel().getSelectedItem();
        project.getUserRoleManagement().getUserDisciplines().remove(selectedUserDiscipline);
        updateUsers(getSelectedDiscipline());
        project.markStudyModified();
    }

    public void reloadUsers(ActionEvent actionEvent) {
        boolean success = project.loadUserManagement();
        updateUsers(getSelectedDiscipline());
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

    private class SubsystemsViewCellFactory implements Callback<ListView<SubSystemModel>, ListCell<SubSystemModel>> {
        @Override
        public ListCell<SubSystemModel> call(ListView<SubSystemModel> p) {
            ListCell<SubSystemModel> cell = new ListCell<SubSystemModel>() {
                @Override
                protected void updateItem(SubSystemModel model, boolean blank) {
                    super.updateItem(model, blank);
                    if (model != null && !blank) {
                        setText(model.getName());
                    } else {
                        setText(null);
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
                    } else {
                        setText(null);
                    }
                }
            };
            return cell;
        }
    }

    private class UserDisciplineFilter implements Predicate<UserDiscipline> {
        private Discipline filterDiscipline;

        public UserDisciplineFilter(Discipline discipline) {
            this.filterDiscipline = discipline;
        }

        @Override
        public boolean test(UserDiscipline userDiscipline) {
            return userDiscipline.getDiscipline() == filterDiscipline;
        }
    }

    private class DisciplineSubSystemViewCellFactory implements Callback<ListView<DisciplineSubSystem>, ListCell<DisciplineSubSystem>> {
        @Override
        public ListCell<DisciplineSubSystem> call(ListView<DisciplineSubSystem> p) {
            ListCell<DisciplineSubSystem> cell = new ListCell<DisciplineSubSystem>() {
                @Override
                protected void updateItem(DisciplineSubSystem disciplineSubSystem, boolean blank) {
                    super.updateItem(disciplineSubSystem, blank);
                    if (disciplineSubSystem != null && !blank) {
                        SubSystemModel subSystem = disciplineSubSystem.getSubSystem();
                        setText(subSystem.getName());
                    } else {
                        setText(null);
                    }
                }
            };
            return cell;
        }
    }

    private class DisciplineSubsystemFilter implements Predicate<DisciplineSubSystem> {
        private Discipline filterDiscipline;

        public DisciplineSubsystemFilter(Discipline discipline) {
            this.filterDiscipline = discipline;
        }

        @Override
        public boolean test(DisciplineSubSystem disciplineSubSystem) {
            return disciplineSubSystem.getDiscipline() == filterDiscipline;
        }
    }

    private class DisciplineNameCellFactory implements Callback<TableColumn<Discipline, Object>, TableCell<Discipline, Object>> {
        @Override
        public TableCell<Discipline, Object> call(TableColumn<Discipline, Object> param) {
            return new TextFieldTableCell<Discipline, Object>() {
                @Override
                public void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item != null && !empty) {
                        Discipline discipline = (Discipline) getTableRow().getItem();
                        String name = discipline.getName() + (discipline.isBuiltIn() ? " (builtin)" : "");
                        setText(name);
                        if (discipline.isBuiltIn()) {
                            setStyle("-fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-font-weight: normal;");
                        }
                    } else {
                        setText(null);
                    }
                }
            };
        }
    }
}
