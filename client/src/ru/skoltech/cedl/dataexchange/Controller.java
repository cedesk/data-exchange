package ru.skoltech.cedl.dataexchange;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.view.ViewNode;
import ru.skoltech.cedl.dataexchange.structure.view.ViewTreeFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Controller {

    @FXML
    public Button openButton;

    @FXML
    private TreeView<ModelNode> structureTree;

    @FXML
    private TableView<ParameterModel> parameterTable;

    private SystemModel system;
    final static private String INPUT_FILE_NAME = "cedesk-SkoltechSat.xml";

    public void loadTree(ActionEvent actionEvent) throws IOException {
        // openButton.setDisable(true);
        // This if is just a dummy replacement of the final functionality. By the end of
        // the day if there is not local repository, we will need to check out the server
        // one. TODO: Fix the dummy study generation when the versioning part is done.
        BufferedReader br = new BufferedReader(new FileReader(INPUT_FILE_NAME));
        if (br.readLine() == null) {
            System.out.println("No errors, and file empty");
            system = DummySystemBuilder.getSystemModel(3);
        } else {
            system = FileStorage.open(INPUT_FILE_NAME);
        }

        ViewNode rootNode = ViewTreeFactory.getViewTree(system);
        structureTree.setRoot(rootNode);
        structureTree.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<TreeItem<ModelNode>>() {
                    @Override
                    public void changed(ObservableValue<? extends TreeItem<ModelNode>> observable, TreeItem<ModelNode> oldValue, TreeItem<ModelNode> newValue) {
                        Controller.this.displayParameters(newValue.getValue());
                    }
                });
    }

    private void displayParameters(ModelNode modelNode) {
        ObservableList<ParameterModel> data =
                FXCollections.observableArrayList(modelNode.getParameters());
        parameterTable.setItems(data);
    }

    public void saveTree(ActionEvent actionEvent) {
        File outputFile = new File(INPUT_FILE_NAME);

        StudyModel study = new StudyModel();
        study.setSystemModel(system);
        study.setFile(outputFile);

        FileStorage.save(study);
    }

    public void newTree(ActionEvent actionEvent) {
        system = DummySystemBuilder.getSystemModel(3);
        ViewNode rootNode = ViewTreeFactory.getViewTree(system);
        structureTree.setRoot(rootNode);
        structureTree.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<TreeItem<ModelNode>>() {
                    @Override
                    public void changed(ObservableValue<? extends TreeItem<ModelNode>> observable, TreeItem<ModelNode> oldValue, TreeItem<ModelNode> newValue) {
                        Controller.this.displayParameters(newValue.getValue());
                    }
                });
    }
}
