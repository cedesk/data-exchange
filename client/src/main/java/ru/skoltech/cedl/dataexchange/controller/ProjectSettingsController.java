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
import ru.skoltech.cedl.dataexchange.services.UserManagementService;
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
    private CheckBox useOsUserCheckbox;

    @FXML
    private TextField userNameText;

    @FXML
    private CheckBox autoloadOnStartupCheckbox;

    private Project project;
    private ProjectSettingsListener projectSettingsListener;

    public void setProject(Project project) {
        this.project = project;
    }

    public void setProjectSettingsListener(ProjectSettingsListener projectSettingsListener) {
        this.projectSettingsListener = projectSettingsListener;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String projectName = project.getProjectName();
        File projectDataDir = project.getProjectDataDir();
        StudySettings studySettings = studySettings();

        teamSettingsPane.setDisable(studySettings == null);

        projectNameText.setText(projectName);
        enableSyncCheckbox.setSelected(studySettings != null && studySettings.getSyncEnabled());
        enableSyncCheckbox.setDisable(studySettings == null);

        useOsUserCheckbox.setSelected(project.getApplicationSettings().isProjectUseOsUser());
        userNameText.disableProperty().bind(useOsUserCheckbox.selectedProperty());
        userNameText.setText(project.getApplicationSettings().getProjectUserName());
        autoloadOnStartupCheckbox.setSelected(project.getApplicationSettings().isProjectLastAutoload());

        projectDirectoryText.setText(projectDataDir.getAbsolutePath());
    }

    private StudySettings studySettings() {
        if (project != null && project.getStudy() != null) {
            if (project.isCurrentAdmin()) {
                return project.getStudy().getStudySettings();
            }
        }
        return null;
    }

    public void applyAndClose(ActionEvent actionEvent) {
        if (projectSettingsListener == null) {
            close(actionEvent);
            return;
        }

        boolean repositoryWatcherAutosync = enableSyncCheckbox.isSelected();
        boolean projectLastAutoload = autoloadOnStartupCheckbox.isSelected();
        boolean projectUseOsUser = useOsUserCheckbox.isSelected();
        String projectUserName = userNameText.getText();

        if (!projectSettingsListener.projectSettingsChanged(repositoryWatcherAutosync, projectLastAutoload,
                projectUseOsUser, projectUserName)) {
            return;
        }

        logger.info("applied");
        close(actionEvent);
    }

    public void cancel(ActionEvent actionEvent) {
        close(actionEvent);
        logger.info("canceled");
    }

    private void close(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        logger.info("closed");
    }

    public void cleanupProjectCache(ActionEvent actionEvent) {
        project.getExternalModelFileHandler().cleanupCache(project);
    }

    /**
     * Is called then user has been new applied changes.
     */
    public interface ProjectSettingsListener {
        boolean projectSettingsChanged(boolean repositoryWatcherAutosync, boolean projectLastAutoload,
                                       boolean projectUseOsUser, String projectUserName);
    }

}
