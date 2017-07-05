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
import ru.skoltech.cedl.dataexchange.ApplicationSettings;
import ru.skoltech.cedl.dataexchange.db.DatabaseRepository;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Repository settings dialog.
 *
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

    private ApplicationSettings applicationSettings;
    private RepositorySettingsListener repositorySettingsListener;

    public RepositorySettingsController(ApplicationSettings applicationSettings) {
        this.applicationSettings = applicationSettings;
    }

    public void setRepositorySettingsListener(RepositorySettingsListener repositorySettingsListener) {
        this.repositorySettingsListener = repositorySettingsListener;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String appDir = StorageUtils.getAppDir().getAbsolutePath();
        appDirText.setText(appDir);
        repoSchemaText.setText(applicationSettings.getRepositorySchema(DatabaseRepository.DEFAULT_SCHEMA));

        dbHostnameText.setText(applicationSettings.getRepositoryServerHostname(DatabaseRepository.DEFAULT_HOST_NAME));
        dbUsernameText.setText(applicationSettings.getRepositoryUserName(""));
        dbPasswordText.setText(applicationSettings.getRepositoryPassword(""));
        repoWatcherAutoSyncCheckbox.setSelected(applicationSettings.getAutoSync());
        logger.info("initialized");
    }

    public void applyAndClose(ActionEvent actionEvent) {
        if (repositorySettingsListener == null) {
            close(actionEvent);
            return;
        }

        boolean autoSynch = repoWatcherAutoSyncCheckbox.isSelected();
        String hostname = dbHostnameText.getText();
        String username = dbUsernameText.getText();
        String password = dbPasswordText.getText();

        if (!repositorySettingsListener.repositorySettingsChanged(hostname, username, password, autoSynch)) {
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

    /**
     * Is called then user has been new applied changes.
     */
    public interface RepositorySettingsListener {
        boolean repositorySettingsChanged(String hostname, String username, String password, boolean autoSynch);
    }
}