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
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.StudySettings;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.service.UserManagementService;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for adjustment of project settings.
 * <p>
 * Created by D.Knoll on 22.07.2015.
 */
public class ProjectSettingsController implements Initializable, Displayable, Closeable {

    private static Logger logger = Logger.getLogger(ProjectSettingsController.class);

    @FXML
    private TitledPane teamSettingsPane;
    @FXML
    private TextField projectDirectoryTextField;
    @FXML
    private TextField projectNameTextField;
    @FXML
    private CheckBox syncEnabledCheckBox;
    @FXML
    private CheckBox projectUseOsUserCheckBox;
    @FXML
    private TextField projectUserNameText;
    @FXML
    private CheckBox projectLastAutoloadCheckBox;
    @FXML
    private Button saveButton;

    private ApplicationSettings applicationSettings;
    private Project project;
    private UserManagementService userManagementService;
    private ExternalModelFileHandler externalModelFileHandler;

    private BooleanProperty changed = new SimpleBooleanProperty(false);
    private Stage ownerStage;

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setUserManagementService(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    public void setExternalModelFileHandler(ExternalModelFileHandler externalModelFileHandler) {
        this.externalModelFileHandler = externalModelFileHandler;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        StudySettings studySettings = studySettings();

        String projectName = project.getProjectName();
        boolean syncEnabled = studySettings != null && studySettings.getSyncEnabled();
        boolean projectUseOsUser = applicationSettings.isProjectUseOsUser();
        String projectUserName = applicationSettings.getProjectUserName();
        boolean projectLastAutoload = applicationSettings.isProjectLastAutoload();

        String projectDataDir = project.getProjectDataDir().getAbsolutePath();

        ChangeListener<Object> changeListener = (observable, oldValue, newValue) ->
                changed.setValue(parametersChanged(syncEnabled, projectUseOsUser,
                        projectUserName, projectLastAutoload));

        teamSettingsPane.setDisable(studySettings == null);

        projectNameTextField.setText(projectName);

        syncEnabledCheckBox.setDisable(studySettings == null);
        syncEnabledCheckBox.setSelected(syncEnabled);
        syncEnabledCheckBox.selectedProperty().addListener(changeListener);

        projectUseOsUserCheckBox.setSelected(projectUseOsUser);
        projectUseOsUserCheckBox.selectedProperty().addListener(changeListener);

        projectUserNameText.disableProperty().bind(projectUseOsUserCheckBox.selectedProperty());
        projectUserNameText.setText(projectUserName);
        projectUserNameText.textProperty().addListener(changeListener);

        projectLastAutoloadCheckBox.setSelected(projectLastAutoload);
        projectLastAutoloadCheckBox.selectedProperty().addListener(changeListener);

        projectDirectoryTextField.setText(projectDataDir);

        saveButton.disableProperty().bind(Bindings.not(changed));
    }

    private boolean parametersChanged(boolean repositoryWatcherAutosync, boolean projectUseOsUser,
                                      String projectUserName, boolean projectLastAutoload) {
        boolean newRepositoryWatcherAutosync = syncEnabledCheckBox.isSelected();
        boolean newProjectUseOsUser = projectUseOsUserCheckBox.isSelected();
        String newProjectUserName = projectUserNameText.getText();
        boolean newProjectLastAutoload = projectLastAutoloadCheckBox.isSelected();
        return repositoryWatcherAutosync != newRepositoryWatcherAutosync
                || projectUseOsUser != newProjectUseOsUser
                || !projectUserName.equals(newProjectUserName)
                || projectLastAutoload != newProjectLastAutoload;
    }


    private StudySettings studySettings() {
        if (project != null && project.getStudy() != null) {
            if (project.checkAdminUser()) {
                return project.getStudy().getStudySettings();
            }
        }
        return null;
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    public void save() {
        if (!changed.getValue()) {
            this.close();
            return;
        }

        boolean repositoryWatcherAutosync = syncEnabledCheckBox.isSelected();
        boolean projectUseOsUser = projectUseOsUserCheckBox.isSelected();
        String projectUserName = projectUserNameText.getText();
        boolean projectLastAutoload = projectLastAutoloadCheckBox.isSelected();

        StudySettings studySettings = studySettings();
        if (studySettings != null) {
            boolean oldSyncEnabled = studySettings.getSyncEnabled();
            studySettings.setSyncEnabled(repositoryWatcherAutosync);
            if (oldSyncEnabled != repositoryWatcherAutosync) {
                project.markStudyModified();
            }
            logger.info(studySettings);
        }

        applicationSettings.storeProjectLastAutoload(projectLastAutoload);
        applicationSettings.storeProjectUseOsUser(projectUseOsUser);

        String userName;
        if (projectUseOsUser) {
            applicationSettings.storeProjectUserName(null);
            userName = applicationSettings.getDefaultProjectUserName(); // get default value
        } else {
            userName = projectUserName;
        }
        boolean validUser = userManagementService.checkUserName(project.getUserManagement(), userName);
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

    public void cancel() {
        this.close();
    }

    @Override
    public void close(Stage stage, WindowEvent windowEvent) {
        this.close();
    }

    public void close() {
        ownerStage.close();
        logger.info("closed");
    }

    public void cleanupProjectCache() {
        externalModelFileHandler.cleanupCache();
    }

}
