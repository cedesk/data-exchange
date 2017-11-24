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

import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import ru.skoltech.cedl.dataexchange.entity.Component;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.model.*;
import ru.skoltech.cedl.dataexchange.service.ComponentService;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static ru.skoltech.cedl.dataexchange.entity.ParameterNature.INPUT;
import static ru.skoltech.cedl.dataexchange.entity.ParameterNature.OUTPUT;

/**
 * Controller for component library window.
 * <p>
 * Created by Dominik Knoll on 06-Nov-17.
 */
public class LibraryController implements Initializable, Displayable {

    private static final String EMPTY_CATEGORY_ITEM = "- none -";

    @FXML
    private TextField searchTextField;
    @FXML
    private ChoiceBox<String> categoryChoice;
    @FXML
    private ListView<Component> componentListView;
    @FXML
    private Button deleteComponentButton;
    @FXML
    private ToolBar toolbar;

    private Stage ownerStage;
    private Consumer<Void> closeEventHandler = aVoid -> ownerStage.close();

    private ListProperty<Component> componentListProperty = new SimpleListProperty<>(FXCollections.emptyObservableList());
    private ListProperty<String> categoryListProperty = new SimpleListProperty<>(FXCollections.emptyObservableList());

    private ComponentService componentService;

    public void setComponentService(ComponentService componentService) {
        this.componentService = componentService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryChoice.itemsProperty().bind(categoryListProperty);
        componentListView.setCellFactory(new ModelNodeViewCellFactory());
        deleteComponentButton.disableProperty().bind(componentListView.getSelectionModel().selectedItemProperty().isNull());

        componentListView.itemsProperty().bind(Bindings.createObjectBinding(() -> {
            List<Component> filteredComponents = filterComponents();
            return FXCollections.observableList(filteredComponents);
        }, searchTextField.textProperty(), categoryChoice.valueProperty(), componentListProperty));
        this.refreshComponents();
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {
        this.ownerStage = stage;
    }

    @FXML
    public void close() {
        this.closeEventHandler.accept(null);
    }

    public void setCloseEventHandler(Consumer<Void> closeEventHandler) {
        Objects.requireNonNull(closeEventHandler);
        this.closeEventHandler = closeEventHandler;
    }

    public void addToolbarButton(Button button) {
        Objects.requireNonNull(button);
        this.toolbar.getItems().add(2, button);
    }

    public ReadOnlyObjectProperty<Component> selectedItemProperty() {
        return componentListView.getSelectionModel().selectedItemProperty();
    }

    @FXML
    public void deleteComponent() {
        Optional<ButtonType> saveOkCancel = Dialogues.chooseOkCancel("Removing component",
                "Are you sure to remove a component?");
        if (saveOkCancel.isPresent() && saveOkCancel.get() == ButtonType.OK) {
            Component selectedItem = componentListView.getSelectionModel().getSelectedItem();
            componentService.deleteComponent(selectedItem);
            refreshComponents();
            if (selectedItem != null) {
                componentListView.getItems().remove(selectedItem);
            }
        }
    }

    public void refreshComponents() {
        List<Component> components = componentService.findComponents();
        componentListProperty.setValue(FXCollections.observableList(components));

        List<String> categoryNames = componentService.findCategories();
        categoryNames.add(0, EMPTY_CATEGORY_ITEM);

        String selectedCategory = categoryChoice.getSelectionModel().getSelectedItem();
        categoryListProperty.setValue(FXCollections.observableList(categoryNames));
        if (categoryNames.stream().anyMatch(s -> s.equals(selectedCategory))) {
            categoryChoice.getSelectionModel().select(selectedCategory);
        } else {
            categoryChoice.getSelectionModel().selectFirst();
        }
    }

    private List<Component> filterComponents() {
        ObservableList<Component> components = componentListProperty.getValue();
        String searchText = searchTextField.getText();
        String category = categoryChoice.getValue();
        if (searchText != null && !searchText.isEmpty()) {
            components = components.stream()
                    .filter(component -> component.getModelNode().getName().toLowerCase().startsWith(searchText.toLowerCase()))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), FXCollections::observableArrayList));
        }
        if (category != null && !category.isEmpty() && !EMPTY_CATEGORY_ITEM.equals(category)) {
            components = components.stream()
                    .filter(component -> component.getCategory().equals(category))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), FXCollections::observableArrayList));
        }
        return components;
    }

    private String getModelType(ModelNode model) {
        if (model instanceof SystemModel) {
            return "System";
        } else if (model instanceof SubSystemModel) {
            return "Subsystem";
        } else if (model instanceof ElementModel) {
            return "Element";
        } else if (model instanceof InstrumentModel) {
            return "Instrument";
        }
        return "";
    }

    private class ModelNodeViewCellFactory implements Callback<ListView<Component>, ListCell<Component>> {
        @Override
        public ListCell<Component> call(ListView<Component> p) {
            AtomicReference<ListCell<Component>> cell = new AtomicReference<>(new ListCell<Component>() {
                @Override
                protected void updateItem(Component component, boolean blank) {
                    super.updateItem(component, blank);
                    if (component != null && !blank) {
                        ModelNode model = component.getModelNode();
                        String inputNames = model.getParameters().stream()
                                .filter(pm -> pm.getNature() == INPUT).map(ParameterModel::getName).sorted()
                                .collect(Collectors.joining(", "));
                        String outputNames = model.getParameters().stream()
                                .filter(pm -> pm.getNature() == OUTPUT).map(ParameterModel::getName).sorted()
                                .collect(Collectors.joining(", "));
                        String modelType = getModelType(model);
                        setText(model.getName() + " (" + modelType + ") [" + component.getAuthor() + "]\n" +
                                "\tinputs: " + inputNames + "\n\toutputs: " + outputNames);
                    } else {
                        setText(null);
                    }
                }
            });
            return cell.get();
        }
    }

}
