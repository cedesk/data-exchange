/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import ru.skoltech.cedl.dataexchange.controller.Dialogues;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.users.UserRoleUtil;

/**
 * Created by D.Knoll on 24.04.2015.
 */
public class TextFieldTreeCell extends TreeCell<ModelNode> {

    private Project project;
    private TextField textField;

    public TextFieldTreeCell(Project project, boolean editable) {
        this.project = project;
        this.setEditable(editable);
    }

    @Override
    public void startEdit() {
        if (!isEditable()) return;
        super.startEdit();

        if (textField == null) {
            createTextField();
        }
        textField.setText(getString());
        setText(null);
        setGraphic(textField);
        textField.selectAll();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getString());
        setGraphic(getTreeItem().getGraphic());
    }

    @Override
    public void updateItem(ModelNode item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getString());
                boolean access = UserRoleUtil.checkAccess(item, project.getUser(), project.getUserRoleManagement());
                if (access) {
                    setStyle("-fx-font-weight: bold;");
                } else {
                    setStyle("-fx-font-weight: normal;");
                }
                setGraphic(getTreeItem().getGraphic());
            }
        }
    }

    private void createTextField() {
        textField = new TextField();
        textField.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.ENTER) {
                    String newName = textField.getText();
                    TreeItem<ModelNode> parent = getTreeItem().getParent();
                    if (parent != null) {
                        CompositeModelNode parentNode = (CompositeModelNode) parent.getValue();
                        if (parentNode.getSubNodesMap().containsKey(newName)) {
                            Dialogues.showError("Duplicate node name", "There is already a sibling node named like that!");
                            return;
                        }
                    }
                    getItem().setName(newName);
                    commitEdit(getItem());
                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            }
        });
    }

    private String getString() {
        return getItem() == null ? "" : getItem().getName();
    }
}
