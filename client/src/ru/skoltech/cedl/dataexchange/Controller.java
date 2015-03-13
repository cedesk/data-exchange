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

import java.io.File;

public class Controller {

    @FXML
    public Button openButton;

    @FXML
    private TreeView<ModelNode> structureTree;

    @FXML
    private TableView<ParameterModel> parameterTable;

    private SystemModel system;

    public void setParameterTable(TableView<ParameterModel> parameterTable) {
        this.parameterTable = parameterTable;
    }

    public void loadTree(ActionEvent actionEvent) {
        openButton.setDisable(true);
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

    private void displayParameters(ModelNode modelNode) {
        ObservableList<ParameterModel> data =
                FXCollections.observableArrayList(modelNode.getParameters());
        parameterTable.setItems(data);
    }

    public void saveTree(ActionEvent actionEvent) {
        File outputFile = new File("cedesk-SkoltechSat.xml");

        StudyModel study = new StudyModel();
        study.setSystemModel(system);
        study.setFile(outputFile);

        FileStorage.save(study);
    }
}
