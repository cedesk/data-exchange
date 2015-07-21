package ru.skoltech.cedl.dataexchange.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;

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

        dbHostnameText.setText(ApplicationSettings.getRepositoryServerHostname(null));
        dbUsernameText.setText(ApplicationSettings.getRepositoryUserName(null));
        dbPasswordText.setText(ApplicationSettings.getRepositoryPassword(null));
    }
}
