/*
 * Copyright 2017 Skolkovo Institute of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import ru.skoltech.cedl.dataexchange.controller.Dialogues;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.entity.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;

/**
 * Created by D.Knoll on 24.04.2015.
 */
public class TextFieldTreeCell extends TreeCell<ModelNode> {

    private Project project;
    private UserRoleManagementService userRoleManagementService;
    private TextField textField;

    public TextFieldTreeCell(Project project, UserRoleManagementService userRoleManagementService, boolean editable) {
        this.project = project;
        this.userRoleManagementService = userRoleManagementService;
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
                UserRoleManagement userRoleManagement = project.getUserRoleManagement();
                User user = project.getUser();
                boolean access = userRoleManagementService.checkUserAccessToModelNode(userRoleManagement, user, item);
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
