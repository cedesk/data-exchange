package ru.skoltech.cedl.dataexchange;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import ru.skoltech.cedl.dataexchange.structure.model.*;
import ru.skoltech.cedl.dataexchange.structure.model.SystemModel;
import ru.skoltech.cedl.dataexchange.structure.view.ViewMapper;

public class Controller {

    @FXML
    private TreeView<String> structureTree;

    @FXML
    private TableView<?> nodePropertyTable;


    public void loadTree(ActionEvent actionEvent) {
        SystemModel system = DummySystemBuilder.getSystemModel();

        TreeItem<String> rootNode = ViewMapper.asItemTree(system);

        structureTree.setRoot(rootNode);
    }

    public void saveTree(ActionEvent actionEvent) {
    }
}
