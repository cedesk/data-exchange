package ru.skoltech.cedl.dataexchange.structure.view;

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import ru.skoltech.cedl.dataexchange.ProjectContext;
import ru.skoltech.cedl.dataexchange.controller.Dialogues;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.CompositeModelNode;
import ru.skoltech.cedl.dataexchange.structure.model.ModelNode;
import ru.skoltech.cedl.dataexchange.users.UserRoleUtil;
import ru.skoltech.cedl.dataexchange.users.model.Discipline;
import ru.skoltech.cedl.dataexchange.users.model.User;

/**
 * Created by D.Knoll on 24.04.2015.
 */
public class TextFieldTreeCell extends TreeCell<ModelNode> {

    private TextField textField;

    public TextFieldTreeCell() {
    }

    @Override
    public void startEdit() {
        super.startEdit();

        if (textField == null) {
            createTextField();
        }
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
                Project project = ProjectContext.getInstance().getProject();
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
        textField = new TextField(getString());
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
