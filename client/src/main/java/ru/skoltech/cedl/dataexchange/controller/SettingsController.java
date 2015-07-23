package ru.skoltech.cedl.dataexchange.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.ProjectContext;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateView();
    }

    private void updateView() {
        autoloadOnStartupCheckbox.setSelected(ApplicationSettings.getAutoLoadLastProjectOnStartup());

        dbHostnameText.setText(ApplicationSettings.getRepositoryServerHostname(""));
        dbUsernameText.setText(ApplicationSettings.getRepositoryUserName(""));
        dbPasswordText.setText(ApplicationSettings.getRepositoryPassword(""));
    }

    public void apply(ActionEvent actionEvent) {
        updateModel();
    }

    private void updateModel() {
        ApplicationSettings.setAutoLoadLastProjectOnStartup(autoloadOnStartupCheckbox.isSelected());

        String hostname = dbHostnameText.getText();
        ApplicationSettings.setRepositoryServerHostname(hostname.isEmpty() ? null : hostname);
        String username = dbUsernameText.getText();
        ApplicationSettings.setRepositoryUserName(username.isEmpty() ? null : username);
        String password = dbPasswordText.getText();
        ApplicationSettings.setRepositoryPassword(password.isEmpty() ? null : password);
        // TODO: first verify credentials, then save them to the application settings
        try {
            ProjectContext.getInstance().getProject().connectRepository();
        } catch (Exception e) {
            Dialogues.showError("Repository connection failed!", "Please verify that the access credentials for the repository are correct.");
        }
    }

    public void cancel(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
