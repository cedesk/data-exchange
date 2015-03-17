package ru.skoltech.cedl.dataexchange;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.tmatesoft.svn.core.SVNException;
import ru.skoltech.cedl.dataexchange.repository.FileStorage;
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.repository.svn.RepositoryStorage;
import ru.skoltech.cedl.dataexchange.structure.model.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.structure.view.ViewNode;
import ru.skoltech.cedl.dataexchange.structure.view.ViewTreeFactory;

import java.io.File;
import java.io.IOException;

public class Controller {

    final static private String REPOSITORY_URL = "file:///C:/Users/d.knoll/SIRG/CedeskRepo";

    @FXML
    private TreeView<ModelNode> structureTree;

    @FXML
    private TableView<ParameterModel> parameterTable;

    private SystemModel system;

    public void newModel(ActionEvent actionEvent) {
        system = DummySystemBuilder.getSystemModel(3);
        ViewNode rootNode = ViewTreeFactory.getViewTree(system);
        structureTree.setRoot(rootNode);
    }

    public void loadModel(ActionEvent actionEvent) {
        // This if is just a dummy replacement of the final functionality. By the end of
        // the day if there is not local repository, we will need to check out the server
        // one. TODO: Fix the dummy study generation when the versioning part is done.
        try {
            File dataFile = StorageUtils.getDataFile();
            if (StorageUtils.fileExistsAndIsNotEmpty(dataFile)) {
                system = FileStorage.load(dataFile);
            } else {
                system = DummySystemBuilder.getSystemModel(4);
            }

            ViewNode rootNode = ViewTreeFactory.getViewTree(system);
            structureTree.setRoot(rootNode);
        } catch (IOException ex) {
            // TODO: message for user on GUI
            System.err.println("Error loading file!");
        }
    }

    public void saveModel(ActionEvent actionEvent) {
        try {
            FileStorage.store(system, StorageUtils.getDataFile());
        } catch (IOException e) {
            // TODO: message for user on GUI
            System.err.println("Error saving file!");
        }
    }

    public void checkoutModel(ActionEvent actionEvent) {
        RepositoryStorage repositoryStorage = null;
        try {
            repositoryStorage = new RepositoryStorage(REPOSITORY_URL, StorageUtils.getDataFileName());
            repositoryStorage.checkoutFile();
        } catch (SVNException e) {
            // TODO: message for user on GUI
            System.err.println("Error connecting to the repository: " + e.getMessage());
        }
    }

    private void displayParameters(ModelNode modelNode) {
        ObservableList<ParameterModel> data =
                FXCollections.observableArrayList(modelNode.getParameters());
        parameterTable.setItems(data);
    }

    public void setStageAndSetupListeners() {
        structureTree.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<TreeItem<ModelNode>>() {
                    @Override
                    public void changed(ObservableValue<? extends TreeItem<ModelNode>> observable, TreeItem<ModelNode> oldValue, TreeItem<ModelNode> newValue) {
                        Controller.this.displayParameters(newValue.getValue());
                    }
                });
    }

}
