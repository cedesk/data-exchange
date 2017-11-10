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

package ru.skoltech.cedl.dataexchange.ui.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.model.ModelNode;
import ru.skoltech.cedl.dataexchange.entity.model.SubSystemModel;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static ru.skoltech.cedl.dataexchange.entity.ParameterNature.INPUT;
import static ru.skoltech.cedl.dataexchange.entity.ParameterNature.OUTPUT;

/**
 * Controller for component library window.
 * <p>
 * Created by Dominik Knoll on 06-Nov-17.
 */
public class LibraryController implements Initializable, Displayable {

    @FXML
    private Button addComponentButton;
    @FXML
    private Button deleteComponentButton;
    @FXML
    private ListView<ModelNode> componentList;
    @FXML
    private TextField searchTextField;
    @FXML
    private ChoiceBox<String> categoryChoice;
    @FXML
    private TextField keywordText;

    private Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    public void deleteComponent() {
        ModelNode selectedItem = componentList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            componentList.getItems().remove(selectedItem);
        }
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        // DUMMY DATA
        List<SubSystemModel> subSystemModels = project.getStudy().getSystemModel().getSubNodes();
        List<String> categoryNames = subSystemModels.stream().map(ModelNode::getName).collect(Collectors.toList());
        categoryNames.add(0, "- none - ");

        categoryChoice.setItems(FXCollections.observableList(categoryNames));
        categoryChoice.setValue(categoryNames.get(0));

        List<ModelNode> modelNodes = subSystemModels.stream()
                .map(subSystemModel -> (ModelNode) subSystemModel).collect(Collectors.toList());
        componentList.setItems(FXCollections.observableList(modelNodes));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        deleteComponentButton.disableProperty().bind(componentList.getSelectionModel().selectedItemProperty().isNull());
        componentList.setCellFactory(new ModelNodeViewCellFactory());
    }

    private class ModelNodeViewCellFactory implements Callback<ListView<ModelNode>, ListCell<ModelNode>> {
        @Override
        public ListCell<ModelNode> call(ListView<ModelNode> p) {
            AtomicReference<ListCell<ModelNode>> cell = new AtomicReference<>(new ListCell<ModelNode>() {
                @Override
                protected void updateItem(ModelNode model, boolean blank) {
                    super.updateItem(model, blank);
                    if (model != null && !blank) {
                        String inputNames = model.getParameters().stream()
                                .filter(pm -> pm.getNature() == INPUT).map(ParameterModel::getName).sorted()
                                .collect(Collectors.joining(", "));
                        String outputNames = model.getParameters().stream()
                                .filter(pm -> pm.getNature() == OUTPUT).map(ParameterModel::getName).sorted()
                                .collect(Collectors.joining(", "));

                        setText(model.getName() + " Instrument 1\n\tinputs: " + inputNames + "\n\toutputs: " + outputNames);
                    } else {
                        setText(null);
                    }
                }
            });
            return cell.get();
        }
    }

}
