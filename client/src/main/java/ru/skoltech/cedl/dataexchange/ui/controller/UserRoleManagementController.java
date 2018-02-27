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
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.Identifiers;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.entity.user.*;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.service.UserService;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
    public TableView<Discipline> disciplinesTable;
    @FXML
    public TableView<User> userTable;
    @FXML
    public Button addDisciplineButton;
    @FXML
    public Button deleteDisciplineButton;
    @FXML
    public ListView<DisciplineSubSystem> subsystemsAssignedList;
    @FXML
    public ListView<SubSystemModel> subsystemsAvailableList;
    @FXML
    public Button addSubsystemButton;
    @FXML
    public Button deleteSubsystemButton;
    @FXML
    public TableColumn<Discipline, Object> disciplineNameColumn;
    @FXML
    public TableColumn<Object, String> disciplineDescriptionColumn;
    @FXML
    public TableColumn<Object, String> subsystemCountColumn;
    @FXML
    public Pane subsystemsPane;
    @FXML
    public ListView<UserDiscipline> userRolesAssignedList;
    @FXML
    public Button addUserRoleButton;
    @FXML
    public Button deleteUserRoleButton;
    @FXML
    public TextField filterTextField;

    private BooleanProperty changed = new SimpleBooleanProperty(false);

    private List<User> users = new LinkedList<>();
    private ListProperty<User> userListProperty = new SimpleListProperty<>(FXCollections.emptyObservableList());


    private Project project;
    private UserService userService;
    private UserRoleManagementService userRoleManagementService;
    private StatusLogger statusLogger;

    public void setProject(Project project) {
        this.project = project;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setUserRoleManagementService(UserRoleManagementService userRoleManagementService) {
        this.userRoleManagementService = userRoleManagementService;
    }

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // DISCIPLINE TABLE
        disciplinesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateSubsystems(newValue);
                updateUserDisciplines(newValue);
            }
        });
        disciplineNameColumn.setCellFactory(new DisciplineNameCellFactory());
        Callback<TableColumn<Object, String>, TableCell<Object, String>> tableCellCallback = TextFieldTableCell.forTableColumn();
        disciplineDescriptionColumn.setCellFactory(tableCellCallback);
        subsystemCountColumn.setCellFactory(tableCellCallback);
        subsystemCountColumn.setCellValueFactory(param -> {
            Discipline discipline = (Discipline) param.getValue();
            if (discipline != null) {
                UserRoleManagement userRoleManagement = project.getUserRoleManagement();
                List<SubSystemModel> subSystemModels = userRoleManagementService.obtainSubSystemsOfDiscipline(userRoleManagement, discipline);
                long subSystemsCount = subSystemModels.size();
                return new SimpleStringProperty(String.valueOf(subSystemsCount));
            } else {
                return new SimpleStringProperty("");
            }
        });
        BooleanBinding noSelectionOnDisciplinesTable = disciplinesTable.getSelectionModel().selectedItemProperty().isNull();
        deleteDisciplineButton.disableProperty().bind(noSelectionOnDisciplinesTable);

        // SUB-SYSTEMS
        subsystemsPane.disableProperty().bind(noSelectionOnDisciplinesTable);

        subsystemsAvailableList.setCellFactory(new SubsystemsViewCellFactory());
        subsystemsAvailableList.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                UserRoleManagementController.this.addDisciplineSubsystem();
            }
        });

        subsystemsAssignedList.setCellFactory(new DisciplineSubSystemViewCellFactory());
        subsystemsAssignedList.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                UserRoleManagementController.this.deleteDisciplineSubsystem();
            }
        });

        BooleanBinding noSelectionOnAvailableSubsystems = subsystemsAvailableList.getSelectionModel().selectedItemProperty().isNull();
        addSubsystemButton.disableProperty().bind(noSelectionOnAvailableSubsystems);
        BooleanBinding noSelectionOnAssignedSubsystems = subsystemsAssignedList.getSelectionModel().selectedItemProperty().isNull();
        deleteSubsystemButton.disableProperty().bind(noSelectionOnAssignedSubsystems);

        // USER-ROLES
        userRolesAssignedList.disableProperty().bind(noSelectionOnDisciplinesTable);
        userRolesAssignedList.setCellFactory(new UserDisciplineViewCellFactory());
        userRolesAssignedList.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                UserRoleManagementController.this.deleteUserRole();
            }
        });

        BooleanBinding noSelectionOnUserTable = userTable.getSelectionModel().selectedItemProperty().isNull();
        addUserRoleButton.disableProperty().bind(Bindings.or(noSelectionOnUserTable, noSelectionOnDisciplinesTable));
        BooleanBinding noSelectionOnAssignedUsers = userRolesAssignedList.getSelectionModel().selectedItemProperty().isNull();
        deleteUserRoleButton.disableProperty().bind(noSelectionOnAssignedUsers);

        //USERS
        this.updateDisciplineTable();
        this.loadUsers();

        userListProperty.bind(Bindings.createObjectBinding(() -> {
            List<User> filteredUnits = users.stream()
                    .filter(user -> (user.getUserName().toLowerCase().contains(filterTextField.getText().toLowerCase())
                            || (user.getFullName() != null &&
                            user.getFullName().toLowerCase().contains(filterTextField.getText().toLowerCase()))))
                    .collect(Collectors.toList());
            return FXCollections.observableList(filteredUnits);
        }, filterTextField.textProperty()));

        userTable.itemsProperty().bind(userListProperty);
        userTable.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                UserRoleManagementController.this.addUserRole();
            }
        });
    }

    private void loadUsers() {
        this.users.clear();
        this.users.addAll(userService.findAllUsers());
        this.users.sort(Comparator.naturalOrder());
    }

    public void addDiscipline() {
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
                statusLogger.info("added discipline: " + discipline.getName());
            }
        }
        updateDisciplineTable();
    }

    public void addDisciplineSubsystem() {
        SubSystemModel subsystem = subsystemsAvailableList.getSelectionModel().getSelectedItem();
        Discipline discipline = getSelectedDiscipline();
        userRoleManagementService.addDisciplineSubsystem(project.getUserRoleManagement(), discipline, subsystem);
        updateSubsystems(discipline);
        changed.setValue(true);
    }

    public void addUserRole() {
        User user = getSelectedUser();
        Discipline discipline = getSelectedDiscipline();
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(user, "discipline must not be null");
        boolean duplicate = userRoleManagementService.addUserDiscipline(project.getUserRoleManagement(), user, discipline);
        if (!duplicate) {
            statusLogger.info("user '" + user.getUserName() + "' can not be added twice to a discipline '" + discipline.getName() + "'");
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

    public void deleteDiscipline() {
        Discipline selectedDiscipline = getSelectedDiscipline();
        Objects.requireNonNull(selectedDiscipline, "no discipline in table view");
        userRoleManagementService.removeDiscipline(project.getUserRoleManagement(), selectedDiscipline);
        changed.setValue(true);
        statusLogger.info("removed discipline: " + selectedDiscipline.getName());
        updateDisciplineTable();
    }

    public void deleteDisciplineSubsystem() {
        DisciplineSubSystem disciplineSubsystem = subsystemsAssignedList.getSelectionModel().getSelectedItem();
        project.getUserRoleManagement().getDisciplineSubSystems().remove(disciplineSubsystem);
        updateSubsystems(getSelectedDiscipline());
        changed.setValue(true);
    }

    public void deleteUserRole() {
        UserDiscipline selectedUserDiscipline = userRolesAssignedList.getSelectionModel().getSelectedItem();
        project.getUserRoleManagement().getUserDisciplines().remove(selectedUserDiscipline);
        updateUserDisciplines(getSelectedDiscipline());
        changed.setValue(true);
    }

    private void updateDisciplineTable() {
        if (project.getUserRoleManagement() != null) {
            List<Discipline> disciplines = project.getUserRoleManagement().getDisciplines();
            ObservableList<Discipline> disciplineList = FXCollections.observableList(disciplines);
            disciplineList.sort(Comparator.naturalOrder());
            disciplinesTable.setItems(disciplineList);
        }
    }

    private void updateSubsystems(Discipline discipline) {
        if (project.getSystemModel() != null) {
            if (discipline != null) {
                // all discipline-subsystem assignments
                List<DisciplineSubSystem> disciplineSubSystemList = project.getUserRoleManagement().getDisciplineSubSystems();
                ObservableList<DisciplineSubSystem> allDisciplineSubsystems = FXCollections.observableArrayList(disciplineSubSystemList);
                ObservableList<DisciplineSubSystem> assignedSubsystemsList = new FilteredList<>(allDisciplineSubsystems, new DisciplineSubsystemFilter(discipline));
                subsystemsAssignedList.setItems(assignedSubsystemsList);

                // get assigned subsystems
                LinkedList<SubSystemModel> assignedSubsystems = disciplineSubSystemList.stream()
                        .map(DisciplineSubSystem::getSubSystem).distinct()
                        .collect(Collectors.toCollection(LinkedList::new));
                // get all subsystems
                List<SubSystemModel> subsystemsList = new ArrayList<>(project.getSystemModel().getSubNodes());
                // retain only un-assigned subsystems
                subsystemsList.removeAll(assignedSubsystems);
                ObservableList<SubSystemModel> nodesList = FXCollections.observableList(subsystemsList);
                nodesList.sort(Comparator.naturalOrder());
                subsystemsAvailableList.setItems(nodesList);
            }
        }
    }

    private void updateUserDisciplines(Discipline discipline) {
        if (project.getUserRoleManagement() != null && discipline != null) {
            // assigned Users
            List<UserDiscipline> userDisciplineList = project.getUserRoleManagement().getUserDisciplines();
            ObservableList<UserDiscipline> allUserDisciplines = FXCollections.observableArrayList(userDisciplineList);
            ObservableList<UserDiscipline> assignedUsersList = new FilteredList<>(allUserDisciplines, new UserDisciplineFilter(discipline));
            userRolesAssignedList.setItems(assignedUsersList);
        }
    }

    private Discipline getSelectedDiscipline() {
        return disciplinesTable.getSelectionModel().getSelectedItem();
    }

    private User getSelectedUser() {
        return userTable.getSelectionModel().getSelectedItem();
    }


    private class SubsystemsViewCellFactory implements Callback<ListView<SubSystemModel>, ListCell<SubSystemModel>> {
        @Override
        public ListCell<SubSystemModel> call(ListView<SubSystemModel> p) {
            AtomicReference<ListCell<SubSystemModel>> cell = new AtomicReference<>(new ListCell<SubSystemModel>() {
                @Override
                protected void updateItem(SubSystemModel model, boolean blank) {
                    super.updateItem(model, blank);
                    if (model != null && !blank) {
                        setText(model.getName());
                    } else {
                        setText(null);
                    }
                }
            });
            return cell.get();
        }
    }

    private class UserDisciplineViewCellFactory implements Callback<ListView<UserDiscipline>, ListCell<UserDiscipline>> {
        @Override
        public ListCell<UserDiscipline> call(ListView<UserDiscipline> p) {
            return new ListCell<UserDiscipline>() {
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
        }
    }

    private class UserDisciplineFilter implements Predicate<UserDiscipline> {
        private Discipline filterDiscipline;

        private UserDisciplineFilter(Discipline discipline) {
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
            return new ListCell<DisciplineSubSystem>() {
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
        }
    }

    private class DisciplineSubsystemFilter implements Predicate<DisciplineSubSystem> {
        private Discipline filterDiscipline;

        private DisciplineSubsystemFilter(Discipline discipline) {
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
