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

import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import ru.skoltech.cedl.dataexchange.entity.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.structure.DifferenceHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.ui.controller.Dialogues;

/**
 * Created by D.Knoll on 24.04.2015.
 */
public class TextFieldTreeCell extends TreeCell<ModelNode> {

    private Project project;
    private DifferenceHandler differenceHandler;
    private TextField textField;

    public TextFieldTreeCell(Project project, DifferenceHandler differenceHandler) {
        this.project = project;
        this.differenceHandler = differenceHandler;
        this.setEditable(false);
    }

    @Override
    public void startEdit() {
        if (!isEditable()) return;
        super.startEdit();

        if (textField == null) {
            createTextField();
        }
        textField.setText(name());
        this.setText(null);
        this.setGraphic(textField);
        textField.selectAll();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        this.setText(name());
        this.setGraphic(getTreeItem().getGraphic());
    }

    @Override
    public void updateItem(ModelNode item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            this.setText(null);
            this.setGraphic(null);
            this.setStyle(null);
            return;
        }
        if (isEditing()) {
            if (textField != null) {
                textField.setText(name());
            }
            this.setText(null);
            this.setGraphic(textField);
            return;
        }
        this.setText(name());
        this.setStyle(style(item));
        this.setGraphic(getTreeItem().getGraphic());
    }

    private String name() {
        return this.getItem() == null ? "" : this.getItem().getName();
    }

    private String style(ModelNode item) {
        boolean accessible = project.checkUserAccess(item);
        boolean applied = differenceHandler.checkAppliedModelNode(item);
        String fontWeightStyle = accessible ? "-fx-font-weight:bold;" : "-fx-font-weight:normal;";
        String backgroundColorStyle = applied ? "-fx-background-color: #FF6A00;" : "";

        return String.join("", fontWeightStyle, backgroundColorStyle);
    }

    private void createTextField() {
        textField = new TextField();
        textField.setOnKeyReleased(t -> {
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
        });
    }
}
