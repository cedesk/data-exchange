package ru.skoltech.cedl.dataexchange.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 22.07.2015.
 */
public class SettingsController implements Initializable {

    @FXML
    private PasswordField dbPasswordText;

    @FXML
    private TextField dbUsernameText;

    @FXML
    private TextField dbHostnameText;

    @FXML
    private CheckBox autoloadOnStartupCheckbox;

    @FXML
    private ComboBox<Integer> modelDepth;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        modelDepth.setItems(FXCollections.observableArrayList(DummySystemBuilder.getValidModelDepths()));
        updateView();
    }

    private void updateView() {
        autoloadOnStartupCheckbox.setSelected(ApplicationSettings.getAutoLoadLastProjectOnStartup());
        modelDepth.setValue(ApplicationSettings.getStudyModelDepth(DummySystemBuilder.DEFAULT_MODEL_DEPTH));

        dbHostnameText.setText(ApplicationSettings.getRepositoryServerHostname(""));
        dbUsernameText.setText(ApplicationSettings.getRepositoryUserName(""));
        dbPasswordText.setText(ApplicationSettings.getRepositoryPassword(""));
    }

    public void applyAndClose(ActionEvent actionEvent) {
        boolean succcess = updateModel();
        if (succcess) {
            cancel(actionEvent);
        }
    }

    private boolean updateModel() {
        boolean success = false;
        ApplicationSettings.setAutoLoadLastProjectOnStartup(autoloadOnStartupCheckbox.isSelected());
        ApplicationSettings.setStudyModelDepth(modelDepth.getValue());

        String hostname = dbHostnameText.getText().isEmpty() ? DatabaseStorage.DEFAULT_HOST_NAME : dbHostnameText.getText();
        String username = dbUsernameText.getText().isEmpty() ? DatabaseStorage.DEFAULT_USER_NAME : dbUsernameText.getText();
        String password = dbPasswordText.getText().isEmpty() ? DatabaseStorage.DEFAULT_PASSWORD : dbPasswordText.getText();
        boolean validCredentials = DatabaseStorage.checkDatabaseConnection(hostname, username, password);
        if (validCredentials) {
            ApplicationSettings.setRepositoryServerHostname(hostname);
            ApplicationSettings.setRepositoryUserName(username);
            ApplicationSettings.setRepositoryPassword(password);
            try {
                ProjectContext.getInstance().getProject().connectRepository();
                success = true;
                StatusLogger.getInstance().log("Successfully configured repository settings!");
            } catch (Exception e) {
                Dialogues.showError("Repository connection failed!", "Please verify that the access credentials for the repository are correct.");
            }
        } else {
            Dialogues.showError("Repository Connection Failed", "The given database access credentials did not work! Please verify they are correct, the database server is running and the connection is working.");
        }
        return success;
    }

    public void cancel(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
