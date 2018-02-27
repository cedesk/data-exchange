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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.service.UserService;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for adjustment of user settings.
 * <p>
 * Created by D.Knoll on 20.02.2018.
 */
public class UserSettingsController implements Initializable, Displayable, Closeable {

    private static Logger logger = Logger.getLogger(UserSettingsController.class);

    @FXML
    private CheckBox projectUseOsUserCheckBox;
    @FXML
    private TextField projectUserNameText;
    @FXML
    private Button saveButton;

    private ApplicationSettings applicationSettings;
    private UserService userService;

    private BooleanProperty changed = new SimpleBooleanProperty(false);
    private Stage ownerStage;

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void cancel() {
        this.close();
    }

    public void close() {
        ownerStage.close();
        logger.info("closed");
    }

    @Override
    public void close(Stage stage, WindowEvent windowEvent) {
        this.close();
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        boolean projectUseOsUser = applicationSettings.isProjectUseOsUser();
        String projectUserName = applicationSettings.getProjectUserName();

        ChangeListener<Object> changeListener = (observable, oldValue, newValue) ->
                changed.setValue(parametersChanged(projectUseOsUser, projectUserName));

        projectUseOsUserCheckBox.setSelected(projectUseOsUser);
        projectUseOsUserCheckBox.selectedProperty().addListener(changeListener);

        projectUserNameText.disableProperty().bind(projectUseOsUserCheckBox.selectedProperty());
        projectUserNameText.setText(projectUserName);
        projectUserNameText.textProperty().addListener(changeListener);

        saveButton.disableProperty().bind(Bindings.not(changed));
    }

    public void save() {
        if (!changed.getValue()) {
            this.close();
            return;
        }

        boolean projectUseOsUser = projectUseOsUserCheckBox.isSelected();
        applicationSettings.storeProjectUseOsUser(projectUseOsUser);

        String userName;
        if (projectUseOsUser) {
            applicationSettings.storeProjectUserName(null);
            userName = applicationSettings.getDefaultProjectUserName(); // get default value
        } else {
            userName = projectUserNameText.getText();
        }
        boolean validUser = userService.checkUser(userName);
        logger.info("using user: '" + userName + "', valid: " + validUser);
        if (validUser) {
            applicationSettings.storeProjectUserName(userName);
        } else {
            Dialogues.showError("Repository authentication failed!", "Please verify the study user name to be used for the projects.");
        }
        applicationSettings.save();

        logger.info("saved");
        this.close();
    }

    private boolean parametersChanged(boolean projectUseOsUser, String projectUserName) {
        boolean newProjectUseOsUser = projectUseOsUserCheckBox.isSelected();
        String newProjectUserName = projectUserNameText.getText();
        return projectUseOsUser != newProjectUseOsUser
                || !projectUserName.equals(newProjectUserName);
    }

}
