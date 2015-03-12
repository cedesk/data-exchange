package ru.skoltech.cedl.dataexchange;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import ru.skoltech.cedl.dataexchange.structure.model.DummySystemBuilder;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ParameterModel;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.structure.view.ViewNode;
import ru.skoltech.cedl.dataexchange.structure.view.ViewTreeFactory;

public class Controller {

    @FXML
    private TreeView<ModelNode> structureTree;

    @FXML
    private TableView<ParameterModel> parameterTable;


    public void loadTree(ActionEvent actionEvent) {
        SystemModel system = DummySystemBuilder.getSystemModel(2);

        ViewNode rootNode = ViewTreeFactory.getViewTree(system);
        structureTree.setRoot(rootNode);
        structureTree.getSelectionModel().selectedItemProperty()
                .addListener(new ChangeListener<TreeItem<ModelNode>>() {
                    @Override
                    public void changed(ObservableValue<? extends TreeItem<ModelNode>> observable, TreeItem<ModelNode> oldValue, TreeItem<ModelNode> newValue) {
                        System.out.println(newValue.getValue().getName());
                    }
                });
    }

    public void saveTree(ActionEvent actionEvent) {
    }
}
