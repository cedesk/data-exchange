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
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.user.*;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Controller for user's roles management.
 * <p>
 * Created by d.knoll on 10.06.2015.
 */
public class UserRoleManagementController implements Initializable, Closeable {

    private static final Logger logger = Logger.getLogger(UserRoleManagementController.class);

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

    private BooleanProperty changed = new SimpleBooleanProperty(false);

    private Project project;
    private UserRoleManagementService userRoleManagementService;

    public Window getAppWindow() {
        return subsystemsPane.getScene().getWindow();
    }

    public Discipline getSelectedDiscipline() {
        return (Discipline) disciplinesTable.getSelectionModel().getSelectedItem();
    }

    public User getSelectedUser() {
        return (User) userTable.getSelectionModel().getSelectedItem();
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setUserRoleManagementService(UserRoleManagementService userRoleManagementService) {
        this.userRoleManagementService = userRoleManagementService;
    }

    public void addDiscipline(ActionEvent actionEvent) {
        Optional<String> disciplineNameChoice = Dialogues.inputDisciplineName("new-discipline");
        if (disciplineNameChoice.isPresent()) {
            String disciplineName = disciplineNameChoice.get();

            if (!Identifiers.validateNodeName(disciplineName)) {
                Dialogues.showError("Invalid name", Identifiers.getNodeNameValidationDescription());
                return;
            }
            Map<String, Discipline> disciplineMap = userRoleManagementService.disciplineMap(project.getUserRoleManagement());
            if (disciplineMap.containsKey(disciplineName)) {
                Dialogues.showError("Duplicate discipline name", "There is already a discipline named like that!");
            } else {
                Discipline discipline = new Discipline(disciplineName, project.getUserRoleManagement());
                project.getUserRoleManagement().getDisciplines().add(discipline);
                changed.setValue(true);
                StatusLogger.getInstance().log("added discipline: " + discipline.getName());
            }
        }
        updateDisciplineTable();
    }

    public void addDisciplineSubsystem(ActionEvent actionEvent) {
        SubSystemModel subsystem = (SubSystemModel) subsystemsAvailableList.getSelectionModel().getSelectedItem();
        Discipline discipline = getSelectedDiscipline();
        userRoleManagementService.addDisciplineSubsystem(project.getUserRoleManagement(), discipline, subsystem);
        updateSubsystems(discipline);
        changed.setValue(true);
    }

    public void addUserRole(ActionEvent actionEvent) {
        User user = getSelectedUser();
        Discipline discipline = getSelectedDiscipline();
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(user, "discipline must not be null");
        boolean duplicate = userRoleManagementService.addUserDiscipline(project.getUserRoleManagement(), user, discipline);
        if (duplicate) {
            StatusLogger.getInstance().log("user '" + user.getUserName() + "' can not be added twice to a discipline '" + discipline.getName() + "'");
        }
        updateUserDisciplines(discipline);
        changed.setValue(true);
    }

    @Override
    public void close(Stage stage, WindowEvent windowEvent) {
        if (!changed.getValue()) {
            return;
        }
        project.markStudyModified();
    }

    public void deleteDiscipline(ActionEvent actionEvent) {
        Discipline selectedDiscipline = getSelectedDiscipline();
        Objects.requireNonNull(selectedDiscipline, "no discipline in table view");
        userRoleManagementService.removeDiscipline(project.getUserRoleManagement(), selectedDiscipline);
        changed.setValue(true);
        StatusLogger.getInstance().log("removed discipline: " + selectedDiscipline.getName());
        updateDisciplineTable();
    }

    public void deleteDisciplineSubsystem(ActionEvent actionEvent) {
        DisciplineSubSystem disciplineSubsystem = (DisciplineSubSystem) subsystemsAssignedList.getSelectionModel().getSelectedItem();
        project.getUserRoleManagement().getDisciplineSubSystems().remove(disciplineSubsystem);
        updateSubsystems(getSelectedDiscipline());
        changed.setValue(true);
    }

    public void deleteUserRole(ActionEvent actionEvent) {
        UserDiscipline selectedUserDiscipline = (UserDiscipline) userRolesAssignedList.getSelectionModel().getSelectedItem();
        project.getUserRoleManagement().getUserDisciplines().remove(selectedUserDiscipline);
        updateUserDisciplines(getSelectedDiscipline());
        changed.setValue(true);
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
                    updateUserDisciplines(discipline);
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
                    UserRoleManagement userRoleManagement = project.getUserRoleManagement();
                    List<SubSystemModel> subSystemModels = userRoleManagementService.obtainSubSystemsOfDiscipline(userRoleManagement, discipline);
                    long subSystemsCount = subSystemModels.size();
                    return new SimpleStringProperty(String.valueOf(subSystemsCount));
                } else {
                    return new SimpleStringProperty("");
                }
            }
        });
        BooleanBinding noSelectionOnDisciplinesTable = disciplinesTable.getSelectionModel().selectedItemProperty().isNull();
        deleteDisciplineButton.disableProperty().bind(noSelectionOnDisciplinesTable);

        // SUB-SYSTEMS
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
        addUserRoleButton.disableProperty().bind(Bindings.or(noSelectionOnUserTable, noSelectionOnDisciplinesTable));
        BooleanBinding noSelectionOnAssignedUsers = userRolesAssignedList.getSelectionModel().selectedItemProperty().isNull();
        deleteUserRoleButton.disableProperty().bind(noSelectionOnAssignedUsers);
        updateView();
    }

    private void updateDisciplineTable() {
        if (project.getUserRoleManagement() != null) {
            List<Discipline> disciplines = project.getUserRoleManagement().getDisciplines();
            ObservableList<Discipline> disciplineList = FXCollections.observableList(disciplines);
            disciplineList.sort(Comparator.<Discipline>naturalOrder());
            disciplinesTable.setItems(disciplineList);
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

    private void updateUserDisciplines(Discipline discipline) {
        if (project.getUserRoleManagement() != null && discipline != null) {
            // assigned Users
            List<UserDiscipline> userDisciplineList = project.getUserRoleManagement().getUserDisciplines();
            ObservableList allUserDisciplines = FXCollections.observableArrayList(userDisciplineList);
            ObservableList assignedUsersList = new FilteredList<UserDiscipline>(allUserDisciplines, new UserDisciplineFilter(discipline));
            userRolesAssignedList.setItems(assignedUsersList);
        }
    }

    private void updateUsers() {
        if (project.getUserManagement() != null) {
            // all Users
            List<User> allUsers = project.getUserManagement().getUsers();
            ObservableList<User> allUserList = FXCollections.observableList(allUsers);
            allUserList.sort(Comparator.<User>naturalOrder());
            userTable.setItems(allUserList);
        }
    }

    private void updateView() {
        updateDisciplineTable();
        updateUsers();
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
            return new TableCell<Discipline, Object>() {
                @Override
                public void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    Discipline discipline = (Discipline) getTableRow().getItem();
                    if (!empty && discipline != null) {
                        if (discipline.isBuiltIn()) {
                            setText(discipline.getName() + " (builtin)");
                            setStyle("-fx-font-weight: bold;");
                        } else {
                            setText(discipline.getName());
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
