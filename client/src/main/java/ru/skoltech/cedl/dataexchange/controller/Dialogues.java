package ru.skoltech.cedl.dataexchange.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by D.Knoll on 28.03.2015.
 */
public class Dialogues {

    public static final ButtonType NEW_STUDY_BUTTON = new ButtonType("Create new Study");
    public static final ButtonType LOAD_STUDY_BUTTON = new ButtonType("Load existing Study");

    /*
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
            return dialog.showAndWait();
        }
    */

    public static File chooseExportPath() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(StorageUtils.getAppDir());
        directoryChooser.setTitle("Select export path.");
        return directoryChooser.showDialog(null);
    }

    public static File chooseImportFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(StorageUtils.getAppDir());
        fileChooser.setTitle("Select import file.");
        return fileChooser.showOpenDialog(null);
    }

    public static File chooseExternalModelFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(StorageUtils.getAppDir());
        fileChooser.setTitle("Select model file.");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Excel", ".xls"));
        return fileChooser.showOpenDialog(null);
    }

    public static Optional<String> inputModelNodeName(String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle("Node Name");
        dialog.setHeaderText("Please insert a name for the new node.");
        dialog.setContentText("Name");
        return dialog.showAndWait();
    }

    public static Optional<String> inputDisciplineName(String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle("Discipline Name");
        dialog.setHeaderText("Please insert a name for the discipline.");
        dialog.setContentText("Name");
        return dialog.showAndWait();
    }

    public static Optional<String> inputStudyName(String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle("Study Name");
        dialog.setHeaderText("Please insert a name for the new Study.");
        dialog.setContentText("Name");
        return dialog.showAndWait();
    }

    public static Optional<String> inputParameterName(String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle("Parameter Name");
        dialog.setHeaderText("Please insert a name for the new parameter.");
        dialog.setContentText("Name");
        return dialog.showAndWait();
    }

    public static Optional<String> inputUserName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("User Name");
        dialog.setHeaderText("Please insert a name for the new user.");
        dialog.setContentText("Name");
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

    public static void showWarning(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(text);
        alert.showAndWait();
    }

    public static Optional<String> chooseStudy(List<String> studyNames) {
        Objects.requireNonNull(studyNames);
        ChoiceDialog<String> dlg = new ChoiceDialog<>(studyNames.get(0), studyNames);
        dlg.setTitle("Open a Study");
        dlg.setHeaderText("Choose from available studies");
        dlg.setContentText("Study");
        return dlg.showAndWait();
    }

    static Optional<ButtonType> chooseNewOrLoadStudy() {
        Alert repositoryTypeDialog = new Alert(Alert.AlertType.CONFIRMATION);
        repositoryTypeDialog.setTitle("Start with a Study");
        repositoryTypeDialog.setHeaderText("Choose whether to create a new study or load an existing one.");
        repositoryTypeDialog.setContentText(null);
        repositoryTypeDialog.getButtonTypes().setAll(NEW_STUDY_BUTTON, LOAD_STUDY_BUTTON, ButtonType.CANCEL);
        return repositoryTypeDialog.showAndWait();
    }
}
