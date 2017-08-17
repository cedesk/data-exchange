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

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.init.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.service.FileStorageService;
import ru.skoltech.cedl.dataexchange.service.RepositoryConnectionService;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;

/**
 * Repository settings dialog.
 * Requires application restart if settings has been changed.
 *
 * Created by D.Knoll on 22.07.2015.
 */
public class RepositorySettingsController implements Initializable, Displayable, Closeable, Applicable {

    private static Logger logger = Logger.getLogger(RepositorySettingsController.class);

    @FXML
    private TextField repositoryHostTextField;

    @FXML
    private TextField repositoryUserTextField;

    @FXML
    private PasswordField repositoryPasswordTextField;

    @FXML
    private TextField applicationDirectoryTextField;

    @FXML
    private TextField repositorySchemaNameTextField;

    @FXML
    private CheckBox repositoryWatcherAutosyncCheckBox;

    @FXML
    public Text connectionTestText;

    @FXML
    private Button saveButton;

    private RepositoryConnectionService repositoryConnectionService;
    private FileStorageService fileStorageService;
    private ApplicationSettings applicationSettings;
    private Executor executor;

    private BooleanProperty changed = new SimpleBooleanProperty(false);
    private EventHandler<Event> applyEventHandler;
    private Stage ownerStage;

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public void setApplicationSettings(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setRepositoryConnectionService(RepositoryConnectionService repositoryConnectionService) {
        this.repositoryConnectionService = repositoryConnectionService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String applicationDirectory = fileStorageService.applicationDirectory().getAbsolutePath();
        String repositorySchemaName = applicationSettings.getRepositorySchemaName();
        String repositoryHost = applicationSettings.getRepositoryHost();
        String repositoryUser = applicationSettings.getRepositoryUser();
        String repositoryPassword = applicationSettings.getRepositoryPassword();
        boolean repositoryWatcherAutosync = applicationSettings.isRepositoryWatcherAutosync();

        ChangeListener<Object> changeListener = (observable, oldValue, newValue) ->
                changed.setValue(parametersChanged(repositoryHost, repositoryUser, repositoryPassword, repositoryWatcherAutosync));

        repositoryHostTextField.setText(repositoryHost);
        repositoryHostTextField.textProperty().addListener(changeListener);

        repositoryUserTextField.setText(repositoryUser);
        repositoryUserTextField.textProperty().addListener(changeListener);

        repositoryPasswordTextField.setText(repositoryPassword);
        repositoryPasswordTextField.textProperty().addListener(changeListener);

        applicationDirectoryTextField.setText(applicationDirectory);
        repositorySchemaNameTextField.setText(repositorySchemaName);

        repositoryWatcherAutosyncCheckBox.setSelected(repositoryWatcherAutosync);
        repositoryWatcherAutosyncCheckBox.selectedProperty().addListener(changeListener);

        saveButton.disableProperty().bind(Bindings.not(changed));

        this.test();
        logger.info("initialized");
    }

    private boolean parametersChanged(String baseRepositoryHost, String baseRepositoryUser,
                                      String baseRepositoryPassword, boolean baseRepositoryWatcherAutosync) {
        String newRepositoryHost = repositoryHostTextField.getText();
        String newRepositoryUser = repositoryUserTextField.getText();
        String newRepositoryPassword = repositoryPasswordTextField.getText();
        boolean newRepositoryWatcherAutosync = repositoryWatcherAutosyncCheckBox.isSelected();
        return !baseRepositoryHost.equals(newRepositoryHost)
                || !baseRepositoryUser.equals(newRepositoryUser)
                || !baseRepositoryPassword.equals(newRepositoryPassword)
                || baseRepositoryWatcherAutosync != newRepositoryWatcherAutosync;
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    public void save(Event event) {
        if (!changed.getValue()) {
            this.close();
            return;
        }

        boolean connection = testConnection();
        String warning = connection ? "" : "Connection is unavailable with currently provided parameters.\n\n";

        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Apply modification?");

        Text warningText = new Text(warning);
        warningText.setStyle("-fx-fill:red; -fx-font-weight:bold;");
        Text alertText1 = new Text("Applying modification requires ");
        Text alertText2 = new Text("application restart");
        alertText2.setStyle("-fx-fill:red; -fx-font-weight:bold; -fx-underline:true;");
        Text alertText3 = new Text(".\n");

        Text questionText = new Text("Apply modification?");
        alert.getDialogPane().setContent(new TextFlow(warningText, alertText1, alertText2, alertText3, questionText));
        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
        Optional<ButtonType> result = alert.showAndWait();
        if (!result.isPresent()) {
            return;
        }
        if (result.get() == cancelButton) {
            event.consume();
        } else if (result.get() == yesButton) {
            this.apply();
            this.close();
        } else if (result.get() == noButton){
            this.close();
        }
    }

    public void cancel() {
        this.close();
    }

    @Override
    public void close(Stage stage, WindowEvent windowEvent) {
        this.close();
    }

    public void close() {
        this.ownerStage.close();
        logger.info("closed");
    }

    @Override
    public void setOnApply(EventHandler<Event> applyEventHandler) {
        this.applyEventHandler = applyEventHandler;
    }

    private void apply() {
        if (applyEventHandler != null) {
            applyEventHandler.handle(new Event(new EventType<>("APPLY")));
        }

        String repositoryHost = repositoryHostTextField.getText();
        String repositoryUser = repositoryUserTextField.getText();
        String repositoryPassword = repositoryPasswordTextField.getText();
        boolean repositoryWatcherAutosync = repositoryWatcherAutosyncCheckBox.isSelected();

        applicationSettings.storeRepositoryHost(repositoryHost);
        applicationSettings.storeRepositoryUser(repositoryUser);
        applicationSettings.storeRepositoryPassword(repositoryPassword);
        applicationSettings.storeRepositoryWatcherAutosync(repositoryWatcherAutosync);
        applicationSettings.save();

        logger.info("applied");
    }

    public void test() {
        connectionTestText.setText("");

        executor.execute(() -> {
            boolean connection = testConnection();
            Platform.runLater(() -> {
                String text = connection ? "SUCCESS" : "FAILURE";
                String color = connection ? "green" : "red";

                connectionTestText.setStyle("-fx-fill:"+ color +"; -fx-font-weight:bold;");
                connectionTestText.setText(text);
            });
        });
    }

    private boolean testConnection() {
        String repositorySchemaName = applicationSettings.getRepositorySchemaName();
        String repositoryHost = repositoryHostTextField.getText();
        String repositoryUser = repositoryUserTextField.getText();
        String repositoryPassword = repositoryPasswordTextField.getText();

        return repositoryConnectionService.checkRepositoryConnection(repositoryHost, repositorySchemaName,
                repositoryUser, repositoryPassword);
    }
}