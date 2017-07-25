/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

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

    public static File chooseExportPath(File applicationDirectory) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(applicationDirectory);
        directoryChooser.setTitle("Select export path.");
        return directoryChooser.showDialog(null);
    }

    public static File chooseImportFile(File applicationDirectory) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(applicationDirectory);
        fileChooser.setTitle("Select import file.");
        return fileChooser.showOpenDialog(null);
    }

    public static File chooseExternalModelFile(File applicationDirectory) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(applicationDirectory);
        fileChooser.setTitle("Select model file.");
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

    public static Optional<String> inputSubsystemNames(String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle("Subsystem Names");
        dialog.setHeaderText("Please insert the names of the subsystems, separated by comma.");
        dialog.setContentText("Names");
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

    public static Optional<ButtonType> chooseNewOrLoadStudy() {
        Alert repositoryTypeDialog = new Alert(Alert.AlertType.CONFIRMATION);
        repositoryTypeDialog.setTitle("Start with a study");
        repositoryTypeDialog.setHeaderText("Choose whether to create a new study or load an existing one.");
        repositoryTypeDialog.setContentText(null);
        repositoryTypeDialog.getButtonTypes().setAll(NEW_STUDY_BUTTON, LOAD_STUDY_BUTTON, ButtonType.CANCEL);
        return repositoryTypeDialog.showAndWait();
    }

    public static Optional<ButtonType> chooseYesNo(String title, String text) {
        Alert yesNoDialog = new Alert(Alert.AlertType.CONFIRMATION);
        yesNoDialog.setHeaderText(title);
        yesNoDialog.setContentText(text);
        yesNoDialog.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        return yesNoDialog.showAndWait();
    }

    public static Optional<ButtonType> chooseOkCancel(String title, String text) {
        Alert yesNoDialog = new Alert(Alert.AlertType.CONFIRMATION);
        yesNoDialog.setHeaderText(title);
        yesNoDialog.setContentText(text);
        yesNoDialog.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        return yesNoDialog.showAndWait();
    }

    public static void showError(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(title);
        alert.setContentText(text);
        alert.showAndWait();
    }

    public static void showWarning(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(title);
        alert.setContentText(text);
        alert.showAndWait();
    }

    public static Optional<String> chooseStudy(List<String> studyNames) {
        Objects.requireNonNull(studyNames);
        ChoiceDialog<String> dlg = new ChoiceDialog<>(studyNames.get(0), studyNames);
        dlg.setTitle("Choose a study");
        dlg.setHeaderText("Choose from available studies");
        dlg.setContentText("Study");
        return dlg.showAndWait();
    }

    public static Optional<String> chooseStudyBuilder(List<String> builderNames) {
        Objects.requireNonNull(builderNames);
        ChoiceDialog<String> dlg = new ChoiceDialog<>(builderNames.get(0), builderNames);
        dlg.setTitle("Choose a study builder");
        dlg.setHeaderText("Choose from available builders");
        dlg.setContentText("Builder");
        return dlg.showAndWait();
    }
}
