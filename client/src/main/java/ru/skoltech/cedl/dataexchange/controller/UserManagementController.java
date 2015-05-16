package ru.skoltech.cedl.dataexchange.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by dknoll on 14/05/15.
 */
public class UserManagementController implements Initializable {
    public TableView userTable;
    public TableView disciplinesTable;
    public TableColumn userNameColumn;
    private Project project;

    public void addUser(ActionEvent actionEvent) {
    }

    public void deleteUser(ActionEvent actionEvent) {
    }

    public void addDiscipline(ActionEvent actionEvent) {
    }

    public void deleteDiscipline(ActionEvent actionEvent) {
    }

    public void setProject(Project project) {
        this.project = project;
        updateTables();
    }

    private void updateTables() {
        List<User> users = project.getUserManagement().getUsers();
        ObservableList<User> userList = FXCollections.observableList(users);
        userTable.setItems(userList);

        List<Discipline> disciplines = project.getUserManagement().getDisciplines();
        ObservableList<Discipline> disciplineList = FXCollections.observableList(disciplines);
        disciplinesTable.setItems(disciplineList);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // NODE PARAMETER TABLE
        Callback<TableColumn<Object, String>, TableCell<Object, String>> tableCellCallback = TextFieldTableCell.forTableColumn();
        userNameColumn.setCellFactory(tableCellCallback);

    }
}