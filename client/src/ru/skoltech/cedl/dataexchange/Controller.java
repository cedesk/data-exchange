package ru.skoltech.cedl.dataexchange;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.structure.view.ViewTreeFactory;

public class Controller {

    @FXML
    private TreeView<String> structureTree;

    @FXML
    private TableView<ParameterModel> parameterTable;


    public void loadTree(ActionEvent actionEvent) {
        SystemModel system = DummySystemBuilder.getSystemModel(2);

        TreeItem<String> rootNode = ViewTreeFactory.getViewTree(system);
        structureTree.setRoot(rootNode);
        structureTree.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<TreeItem<String>>() {

                    @Override
                    public void changed(
                            ObservableValue<? extends TreeItem<String>> observable,
                            TreeItem<String> oldValue, TreeItem<String> newValue) {
                        System.out.println("Selected Text : " + newValue.getValue());
                    }

                });
    }

    public void saveTree(ActionEvent actionEvent) {
    }
}
