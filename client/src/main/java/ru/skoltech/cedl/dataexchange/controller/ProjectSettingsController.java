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

package ru.skoltech.cedl.dataexchange.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.StudySettings;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for adjustment of project settings.
 *
 * Created by D.Knoll on 22.07.2015.
 */
public class ProjectSettingsController implements Initializable {

    private static Logger logger = Logger.getLogger(ProjectSettingsController.class);

    @FXML
    private TitledPane teamSettingsPane;

    @FXML
    private TextField projectDirectoryText;

    @FXML
    private TextField projectNameText;

    @FXML
    private CheckBox enableSyncCheckbox;

    @FXML
    private CheckBox autoloadOnStartupCheckbox;

    @FXML
    private CheckBox useOsUserCheckbox;

    @FXML
    private TextField userNameText;

    private Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userNameText.disableProperty().bind(useOsUserCheckbox.selectedProperty());
        updateView();
    }

    private StudySettings getStudySettings() {
        if (project != null && project.getStudy() != null) {
            boolean isAdmin = project.isCurrentAdmin();
            if (isAdmin)
                return project.getStudy().getStudySettings();
        }
        return null;
    }

    public void applyAndClose(ActionEvent actionEvent) {
        boolean succcess = updateModel();
        if (succcess) {
            cancel(actionEvent);
        }
    }

    public void cancel(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    public void cleanupProjectCache(ActionEvent actionEvent) {
        project.getExternalModelFileHandler().cleanupCache(project);
    }

    private boolean updateModel() {
        StudySettings studySettings = getStudySettings();
        if (studySettings != null) {
            boolean oldSyncEnabled = studySettings.getSyncEnabled();
            boolean newSyncEnable = enableSyncCheckbox.isSelected();
            studySettings.setSyncEnabled(newSyncEnable);
            if (oldSyncEnabled != newSyncEnable) {
                project.markStudyModified();
            }
            logger.info(studySettings);
        }

        project.getApplicationSettings().setAutoLoadLastProjectOnStartup(autoloadOnStartupCheckbox.isSelected());
        project.getApplicationSettings().setUseOsUser(useOsUserCheckbox.isSelected());
        String userName = null;
        if (useOsUserCheckbox.isSelected()) {
            project.getApplicationSettings().setProjectUser(null);
            userName = project.getApplicationSettings().getProjectUser(); // get default value
        } else {
            userName = userNameText.getText();
        }
        boolean validUser = project.getUserManagement().checkUser(userName);
        logger.info("using user: '" + userName + "', valid: " + validUser);
        if (validUser) {
            project.getApplicationSettings().setProjectUser(userName);
        } else {
            Dialogues.showError("Repository authentication failed!", "Please verify the study user name to be used for the projects.");
        }

        return validUser;
    }

    private void updateView() {
        String projectName = project.getProjectName();
        projectNameText.setText(projectName);
        StudySettings studySettings = getStudySettings();
        if (studySettings != null) {
            enableSyncCheckbox.setSelected(studySettings.getSyncEnabled());
            enableSyncCheckbox.setDisable(false);
            teamSettingsPane.setDisable(false);
        } else {
            enableSyncCheckbox.setDisable(true);
            teamSettingsPane.setDisable(true);
        }

        File projectDataDir = project.getProjectDataDir();
        projectDirectoryText.setText(projectDataDir.getAbsolutePath());

        autoloadOnStartupCheckbox.setSelected(project.getApplicationSettings().getAutoLoadLastProjectOnStartup());
        useOsUserCheckbox.setSelected(project.getApplicationSettings().getUseOsUser());
        userNameText.setText(project.getApplicationSettings().getProjectUser());
    }

}
