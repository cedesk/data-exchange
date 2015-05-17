package ru.skoltech.cedl.dataexchange.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.Optional;

/**
 * Created by D.Knoll on 28.03.2015.
 */
public class Dialogues {

    public static final ButtonType REMOTE_REPO = new ButtonType("Remote");
    public static final ButtonType LOCAL_REPO = new ButtonType("Local");

    static void showInvalidRepositoryWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Repository");
        alert.setHeaderText(null);
        alert.setContentText("There is no repository set yet. You will need to specify one!");
        alert.showAndWait();
    }

    static void showInvalidRepositoryPath() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Repository");
        alert.setContentText("The selected path does not contain a valid repository!");
        alert.showAndWait();
    }

    static Optional<ButtonType> chooseLocalOrRemoteRepository() {
        Alert repositoryTypeDialog = new Alert(Alert.AlertType.CONFIRMATION);
        repositoryTypeDialog.setTitle("Repository type selection");
        repositoryTypeDialog.setHeaderText("Please choose which type of repository you want to use.");
        repositoryTypeDialog.setContentText(null);
        repositoryTypeDialog.getButtonTypes().setAll(REMOTE_REPO, LOCAL_REPO, ButtonType.CANCEL);
        return repositoryTypeDialog.showAndWait();
    }

    static File chooseLocalRepositoryPath() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Repository path");
        return directoryChooser.showDialog(null);
    }

    static Optional<String> inputRemoteRepositoryURL() {
        TextInputDialog dialog = new TextInputDialog("https://");
        dialog.setTitle("Repository URL");
        dialog.setHeaderText("Please insert the URL for the repository. It shall start with 'http' or 'https'.");
        dialog.setContentText("URL:");
        // TODO: add input validation
        return dialog.showAndWait();
    }

    static Optional<String> inputModelNodeName(String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle("Node Name");
        dialog.setHeaderText("Please insert a name for the new node.");
        dialog.setContentText("Name");
        //TODO: add input validation
        return dialog.showAndWait();
    }

    static Optional<String> inputParameterName(String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle("Parameter Name");
        dialog.setHeaderText("Please insert a name for the new parameter.");
        dialog.setContentText("Name");
        //TODO: add input validation
        return dialog.showAndWait();
    }

    static Optional<String> inputUserName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("User Name");
        dialog.setHeaderText("Please insert a name for the new user.");
        dialog.setContentText("Name");
        //TODO: add input validation
        return dialog.showAndWait();
    }

    public static Optional<ButtonType> chooseYesNo(String title, String text) {
        Alert yesNoDialog = new Alert(Alert.AlertType.CONFIRMATION);
        yesNoDialog.setTitle(title);
        yesNoDialog.setHeaderText(text);
        yesNoDialog.setContentText(null);
        yesNoDialog.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        return yesNoDialog.showAndWait();
    }

    public static void showError(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(text);
        alert.showAndWait();
    }

}
