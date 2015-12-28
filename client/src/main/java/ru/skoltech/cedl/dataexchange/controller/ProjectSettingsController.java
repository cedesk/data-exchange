package ru.skoltech.cedl.dataexchange.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.structure.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.StudySettings;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 22.07.2015.
 */
public class ProjectSettingsController implements Initializable {

    private static Logger logger = Logger.getLogger(ProjectSettingsController.class);

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
        useOsUserCheckbox.setSelected(ApplicationSettings.getUseOsUser());
        userNameText.setText(ApplicationSettings.getProjectUser());

    }

    public void applyAndClose(ActionEvent actionEvent) {
        boolean succcess = updateModel();
        if (succcess) {
            cancel(actionEvent);
        }
    }

    private boolean updateModel() {
        boolean validSettings = false;

        StudySettings studySettings = getStudySettings();
        if (studySettings != null) {
            boolean oldSyncEnabled = studySettings.getSyncEnabled();
            boolean newSyncEnable = enableSyncCheckbox.isSelected();
            studySettings.setSyncEnabled(newSyncEnable);
            if (oldSyncEnabled != newSyncEnable) {
                ProjectContext.getInstance().getProject().markStudyModified();
            }
            logger.info(studySettings);
        }

        ApplicationSettings.setAutoLoadLastProjectOnStartup(autoloadOnStartupCheckbox.isSelected());
        ApplicationSettings.setStudyModelDepth(modelDepth.getValue());
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
            boolean isAdmin = project.isCurrentAdmin();
            if (isAdmin)
                return project.getStudy().getStudySettings();
        }
        return null;
    }
}
