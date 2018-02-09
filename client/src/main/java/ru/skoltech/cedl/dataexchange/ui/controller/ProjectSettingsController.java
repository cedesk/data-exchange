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
import ru.skoltech.cedl.dataexchange.entity.model.SystemModel;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.service.UserService;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

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
    private CheckBox saveEnabledCheckBox;
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
    private UserService userService;

    private BooleanProperty changed = new SimpleBooleanProperty(false);
    private Stage ownerStage;

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void cancel() {
        this.close();
    }

    public void cleanupProjectCache() {
        SystemModel systemModel = project.getStudy().getSystemModel();
        Set<String> actualCacheFiles = new HashSet<>();
        File projectDataDir = project.getProjectHome();
        if (!projectDataDir.exists()) {
            return;
        }
        systemModel.externalModelsIterator().forEachRemaining(externalModel -> {
            actualCacheFiles.add(externalModel.getCacheFile().getAbsolutePath());
            logger.info("File: '" + externalModel.getCacheFile().getAbsolutePath() + "', [" + externalModel.state() + "], need to keep");
        });
        // go through cache directory, check if to keep, otherwise delete
        try {
            Files.walk(projectDataDir.toPath(), FileVisitOption.FOLLOW_LINKS).forEach(path -> {
                File file = path.toFile();
                // skip files which are actualCacheFiles and the related .tstamp file
                if (file.isFile() && actualCacheFiles.stream().noneMatch(s -> file.getAbsolutePath().startsWith(s))) {
                    // files not to be kept
                    logger.info("Deleting: '" + file.getAbsolutePath() + "' file");
                    boolean deleted = file.delete();
                    if (!deleted) {
                        logger.warn("Cannot delete '" + file.getAbsolutePath() + "' file");
                    }
                } else if (file.isDirectory() && file.list() != null && file.list().length == 0) { // empty directories
                    logger.info("Deleting: '" + file.getAbsolutePath() + "' directory");
                    boolean deleted = file.delete();
                    if (!deleted) {
                        logger.warn("Cannot delete '" + file.getAbsolutePath() + "' directory");
                    }
                } else {
                    logger.info("Keeping: '" + file.getAbsolutePath() + "'");
                }
            });
        } catch (IOException e) {
            logger.error("Error traversing project directory", e);
        }
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
        String projectName = project.getProjectName();
        boolean noProject = projectName == null || projectName.isEmpty();
        String projectDataDir = noProject ? "" : project.getProjectHome().getAbsolutePath();

        projectNameTextField.setText(projectName);
        projectDirectoryTextField.setText(projectDataDir);

        StudySettings studySettings = studySettings();
        boolean saveEnabled = studySettings == null || studySettings.getSyncEnabled();
        boolean projectUseOsUser = applicationSettings.isProjectUseOsUser();
        String projectUserName = applicationSettings.getProjectUserName();
        boolean projectLastAutoload = applicationSettings.isProjectLastAutoload();

        ChangeListener<Object> changeListener = (observable, oldValue, newValue) ->
                changed.setValue(parametersChanged(saveEnabled, projectUseOsUser, projectUserName, projectLastAutoload));

        teamSettingsPane.setDisable(!project.checkAdminUser());

        saveEnabledCheckBox.setSelected(saveEnabled);
        saveEnabledCheckBox.selectedProperty().addListener(changeListener);

        projectUseOsUserCheckBox.setSelected(projectUseOsUser);
        projectUseOsUserCheckBox.selectedProperty().addListener(changeListener);

        projectUserNameText.disableProperty().bind(projectUseOsUserCheckBox.selectedProperty());
        projectUserNameText.setText(projectUserName);
        projectUserNameText.textProperty().addListener(changeListener);

        projectLastAutoloadCheckBox.setSelected(projectLastAutoload);
        projectLastAutoloadCheckBox.selectedProperty().addListener(changeListener);

        saveButton.disableProperty().bind(Bindings.not(changed));
    }

    public void save() {
        if (!changed.getValue()) {
            this.close();
            return;
        }

        boolean saveEnabled = saveEnabledCheckBox.isSelected();
        boolean projectUseOsUser = projectUseOsUserCheckBox.isSelected();
        String projectUserName = projectUserNameText.getText();
        boolean projectLastAutoload = projectLastAutoloadCheckBox.isSelected();

        StudySettings studySettings = studySettings();
        if (project.checkAdminUser()) {
            boolean oldSaveEnabled = studySettings.getSyncEnabled();
            studySettings.setSyncEnabled(saveEnabled);
            if (oldSaveEnabled != saveEnabled) {
                project.isSyncEnabledProperty().setValue(saveEnabled);
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

    private boolean parametersChanged(boolean saveEnabled, boolean projectUseOsUser,
                                      String projectUserName, boolean projectLastAutoload) {
        boolean newSaveEnabled = saveEnabledCheckBox.isSelected();
        boolean newProjectUseOsUser = projectUseOsUserCheckBox.isSelected();
        String newProjectUserName = projectUserNameText.getText();
        boolean newProjectLastAutoload = projectLastAutoloadCheckBox.isSelected();
        return saveEnabled != newSaveEnabled
                || projectUseOsUser != newProjectUseOsUser
                || !projectUserName.equals(newProjectUserName)
                || projectLastAutoload != newProjectLastAutoload;
    }

    private StudySettings studySettings() {
        if (project != null && project.getStudy() != null) {
            StudySettings studySettings = project.getStudy().getStudySettings();
            if (studySettings == null) {
                studySettings = new StudySettings();
                project.getStudy().setStudySettings(studySettings);
            }
            return studySettings;
        }
        return null;
    }

}
