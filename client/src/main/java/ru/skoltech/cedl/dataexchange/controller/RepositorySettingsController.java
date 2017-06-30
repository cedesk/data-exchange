package ru.skoltech.cedl.dataexchange.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.StatusLogger;
import ru.skoltech.cedl.dataexchange.db.DatabaseRepository;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 22.07.2015.
 */
public class RepositorySettingsController implements Initializable {

    private static Logger logger = Logger.getLogger(RepositorySettingsController.class);

    @FXML
    private CheckBox repoWatcherAutoSyncCheckbox;

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

    private Project project;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void setProject(Project project) {
        this.project = project;
        updateView();
    }

    private void updateView() {
        String appDir = StorageUtils.getAppDir().getAbsolutePath();
        appDirText.setText(appDir);
        repoSchemaText.setText(project.getApplicationSettings().getRepositorySchema(DatabaseRepository.DEFAULT_SCHEMA));

        dbHostnameText.setText(project.getApplicationSettings().getRepositoryServerHostname(DatabaseRepository.DEFAULT_HOST_NAME));
        dbUsernameText.setText(project.getApplicationSettings().getRepositoryUserName(""));
        dbPasswordText.setText(project.getApplicationSettings().getRepositoryPassword(""));
        repoWatcherAutoSyncCheckbox.setSelected(project.getApplicationSettings().getAutoSync());
    }

    public void applyAndClose(ActionEvent actionEvent) {
        boolean success = updateModel();
        if (success) {
            cancel(actionEvent);
        }
    }

    private boolean updateModel() {
        boolean validSettings = false;

        project.getApplicationSettings().setAutoSync(repoWatcherAutoSyncCheckbox.isSelected());

        String schema = project.getApplicationSettings().getRepositorySchema(DatabaseRepository.DEFAULT_SCHEMA);

        String hostname = dbHostnameText.getText().isEmpty() ? DatabaseRepository.DEFAULT_HOST_NAME : dbHostnameText.getText();
        String username = dbUsernameText.getText().isEmpty() ? DatabaseRepository.DEFAULT_USER_NAME : dbUsernameText.getText();
        String password = dbPasswordText.getText().isEmpty() ? DatabaseRepository.DEFAULT_PASSWORD : dbPasswordText.getText();
        boolean validCredentials = DatabaseRepository.checkDatabaseConnection(hostname, schema, username, password);
        if (validCredentials) {
            project.getApplicationSettings().setRepositoryServerHostname(hostname);
            project.getApplicationSettings().setRepositoryUserName(username);
            project.getApplicationSettings().setRepositoryPassword(password);
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
        ProjectContext.getInstance().getProject().loadUserManagement();
        ProjectContext.getInstance().getProject().loadUnitManagement();
        return validSettings;
    }

    public void cancel(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

}
