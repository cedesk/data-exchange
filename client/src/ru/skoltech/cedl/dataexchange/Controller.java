package ru.skoltech.cedl.dataexchange;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.model.System;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Controller {

    @FXML
    private TreeView<String> structureTree;

    @FXML
    private TableView<?> nodePropertyTable;


    public void loadTree(ActionEvent actionEvent) {
        System system = DummySystemBuilder.getSystemModel();

        TreeItem<String> rootNode =
                new TreeItem<>(system.getName(), system.getImage());
        rootNode.setExpanded(true);
        int cnt = 1;
        for(Iterator<SubSystem> iter = system.iterator(); iter.hasNext();){
            ModelNode modelNode = iter.next();
            TreeItem<String> childNode = new TreeItem<>(
                    modelNode.getName(),
                    modelNode.getImage()
            );
            childNode.setExpanded(true);
            childNode.getChildren().add(new TreeItem<>("instrument" + (cnt++)));

            rootNode.getChildren().add(childNode);
        }

        structureTree.setRoot(rootNode);
    }

    public void saveTree(ActionEvent actionEvent) {
    }
}
