package ru.skoltech.cedl.dataexchange.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.db.DatabaseStorage;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.StudySettings;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 22.07.2015.
 */
public class SettingsController implements Initializable {

    @FXML
    public TitledPane studySettingsPane;

    @FXML
    public CheckBox enableSyncCheckbox;

    @FXML
    private CheckBox autoloadOnStartupCheckbox;

    @FXML
    private ComboBox<Integer> modelDepth;

    @FXML
    private CheckBox useOsUserCheckbox;

    @FXML
    private TextField userNameText;

    @FXML
    private TextField appDirText;

    @FXML
    private TextField repoSchemaText;

    @FXML
    private TextField dbHostnameText;

    @FXML
    private TextField dbUsernameText;

    @FXML
    private PasswordField dbPasswordText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        modelDepth.setItems(FXCollections.observableArrayList(DummySystemBuilder.getValidModelDepths()));
        userNameText.disableProperty().bind(useOsUserCheckbox.selectedProperty());
        updateView();
    }

    private void updateView() {
        StudySettings studySettings = getStudySettings();
        if (studySettings != null) {
            enableSyncCheckbox.setSelected(studySettings.getSyncEnabled());
            enableSyncCheckbox.setDisable(false);
            studySettingsPane.setDisable(false);
        } else {
            enableSyncCheckbox.setDisable(true);
            studySettingsPane.setDisable(true);
        }

        autoloadOnStartupCheckbox.setSelected(ApplicationSettings.getAutoLoadLastProjectOnStartup());
        modelDepth.setValue(ApplicationSettings.getStudyModelDepth(DummySystemBuilder.DEFAULT_MODEL_DEPTH));

        String appDir = StorageUtils.getAppDir().getAbsolutePath();
        appDirText.setText(appDir);
        repoSchemaText.setText(ApplicationSettings.getRepositorySchema(DatabaseStorage.DEFAULT_SCHEMA));

        useOsUserCheckbox.setSelected(ApplicationSettings.getUseOsUser());
        userNameText.setText(ApplicationSettings.getProjectUser());

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
        boolean validSettings = false;
        boolean studyChanged = false;

        StudySettings studySettings = getStudySettings();
        if (studySettings != null) {
            boolean oldSyncEnabled = studySettings.getSyncEnabled();
            boolean newSyncEnable = enableSyncCheckbox.isSelected();
            studySettings.setSyncEnabled(newSyncEnable);
            studyChanged = oldSyncEnabled != newSyncEnable;
        }
        if(studyChanged) {
            ProjectContext.getInstance().getProject().markStudyModified();
        }

        ApplicationSettings.setAutoLoadLastProjectOnStartup(autoloadOnStartupCheckbox.isSelected());
        ApplicationSettings.setStudyModelDepth(modelDepth.getValue());

        String schema = ApplicationSettings.getRepositorySchema(DatabaseStorage.DEFAULT_SCHEMA);

        String hostname = dbHostnameText.getText().isEmpty() ? DatabaseStorage.DEFAULT_HOST_NAME : dbHostnameText.getText();
        String username = dbUsernameText.getText().isEmpty() ? DatabaseStorage.DEFAULT_USER_NAME : dbUsernameText.getText();
        String password = dbPasswordText.getText().isEmpty() ? DatabaseStorage.DEFAULT_PASSWORD : dbPasswordText.getText();
        boolean validCredentials = DatabaseStorage.checkDatabaseConnection(hostname, schema, username, password);
        if (validCredentials) {
            ApplicationSettings.setRepositoryServerHostname(hostname);
            ApplicationSettings.setRepositoryUserName(username);
            ApplicationSettings.setRepositoryPassword(password);
            try {
                ProjectContext.getInstance().getProject().connectRepository();
                validSettings = true;
                StatusLogger.getInstance().log("Successfully configured repository settings!");
            } catch (Exception e) {
                Dialogues.showError("Repository connection failed!", "Please verify that the access credentials for the repository are correct.");
            }
        } else {
            Dialogues.showError("Repository Connection Failed", "The given database access credentials did not work! Please verify they are correct, the database server is running and the connection is working.");
        }

        if (!validSettings)
            return false;

        ApplicationSettings.setUseOsUser(useOsUserCheckbox.isSelected());
        if (useOsUserCheckbox.isSelected()) {
            ApplicationSettings.setProjectUser(null);
        } else {
            String userName = userNameText.getText();
            boolean validUser = ProjectContext.getInstance().getProject().getUserManagement().checkUser(userName);
            validSettings = validUser;
            if (validUser) {
                ApplicationSettings.setProjectUser(userName);
            } else {
                Dialogues.showError("Repository authentication failed!", "Please verify the study user name to be used for the projects.");
            }
        }

        return validSettings;
    }

    public void cancel(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private StudySettings getStudySettings() {
        Project project = ProjectContext.getInstance().getProject();
        if (project != null && project.getStudy() != null) {
            boolean isAdmin = project.getUserRoleManagement().isAdmin(project.getUser());
            if (isAdmin)
                return project.getStudy().getStudySettings();
        }
        return null;
    }
}
