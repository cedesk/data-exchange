package ru.skoltech.cedl.dataexchange;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Arrays;
import java.util.List;

public class Controller {

    @FXML
    private TreeView<String> structureTree;

    @FXML
    private TableView<?> nodePropertyTable;

    private final Node sysIcon =
            new ImageView(new Image(getClass().getResourceAsStream("../../../../spacecraft.png")));
    private final Image subIcon =
            new Image(getClass().getResourceAsStream("../../../../subsystem.png"));
    List<ModelNode> modelNodes = Arrays.<ModelNode>asList(
            new ModelNode("Power"),
            new ModelNode("AOCS"),
            new ModelNode("Thermal"),
            new ModelNode("Orbit"),
            new ModelNode("Payload"),
            new ModelNode("Communication"));
    TreeItem<String> systemNode =
            new TreeItem<String>("Space System", sysIcon);

    public void loadTree(ActionEvent actionEvent) {
        systemNode.setExpanded(true);
        int cnt = 1;
        for (ModelNode modelNode : modelNodes) {
            TreeItem<String> subsystemNode = new TreeItem<String>(
                    modelNode.getName(),
                    new ImageView(subIcon)
            );
            subsystemNode.setExpanded(true);
            subsystemNode.getChildren().add(new TreeItem<>("instrument"+(cnt++)));

            systemNode.getChildren().add(subsystemNode);
        }

        structureTree.setRoot(systemNode);
    }

    public void saveTree(ActionEvent actionEvent) {
    }
}
