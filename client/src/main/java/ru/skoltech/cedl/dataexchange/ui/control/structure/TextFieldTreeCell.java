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

package ru.skoltech.cedl.dataexchange.ui.control.structure;

import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.user.Discipline;
import ru.skoltech.cedl.dataexchange.entity.user.User;
import ru.skoltech.cedl.dataexchange.entity.user.UserRoleManagement;
import ru.skoltech.cedl.dataexchange.service.UserRoleManagementService;
import ru.skoltech.cedl.dataexchange.structure.DifferenceHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.model.diff.ModelDifference;
import ru.skoltech.cedl.dataexchange.structure.model.diff.NodeDifference;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 24.04.2015.
 */
public class TextFieldTreeCell extends TreeCell<ModelNode> {

    private Project project;
    private DifferenceHandler differenceHandler;
    private UserRoleManagementService userRoleManagementService;

    public TextFieldTreeCell(Project project, DifferenceHandler differenceHandler, UserRoleManagementService userRoleManagementService) {
        this.project = project;
        this.differenceHandler = differenceHandler;
        this.userRoleManagementService = userRoleManagementService;
        this.setEditable(false);
    }

    @Override
    public void updateItem(ModelNode item, boolean empty) {
        super.updateItem(item, empty);

        String owners = owners(item);
        this.setTooltip(owners != null ? new Tooltip("Owners: " + owners) : null);
        if (empty) {
            this.setText(null);
            this.setGraphic(null);
            this.setStyle(null);
            return;
        }
        this.setText(name());
        this.setStyle(style(item));
        this.setGraphic(getTreeItem().getGraphic());
    }

    private String name() {
        return this.getItem() == null ? "" : this.getItem().getName();
    }

    private String owners(ModelNode modelNode) {
        if (modelNode == null) {
            return null;
        }
        UserRoleManagement userRoleManagement = project.getUserRoleManagement();
        Discipline disciplineOfSubSystem = userRoleManagementService.obtainDisciplineOfSubSystem(userRoleManagement, modelNode);
        List<User> usersOfDiscipline = userRoleManagementService.obtainUsersOfDiscipline(userRoleManagement, disciplineOfSubSystem);
        if (usersOfDiscipline.isEmpty()) return "<none>";
        return usersOfDiscipline.stream().map(User::name).collect(Collectors.joining(", "));
    }

    private String style(ModelNode item) {
        boolean accessible = project.checkUserAccess(item);

        List<NodeDifference> modelNodeDifferences = differenceHandler.modelDifferences().stream()
                .filter(modelDifference -> modelDifference instanceof NodeDifference)
                .filter(modelDifference -> modelDifference.getChangeLocation() == ModelDifference.ChangeLocation.ARG2)
                .map(modelDifference -> (NodeDifference) modelDifference)
                .filter(nodeDifference -> item.getUuid().equals(nodeDifference.getNode1().getUuid()))
                .collect(Collectors.toList());

        boolean applied = !modelNodeDifferences.isEmpty();
        String fontWeightStyle = accessible ? "-fx-font-weight:bold;" : "-fx-font-weight:normal;";
        String backgroundColorStyle = applied ? "-fx-background-color: #FF6A00;" : "";

        return String.join("", fontWeightStyle, backgroundColorStyle);
    }
}
