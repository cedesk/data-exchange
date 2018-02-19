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

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.tradespace.FigureOfMeritDefinition;
import ru.skoltech.cedl.dataexchange.entity.tradespace.MultitemporalTradespace;
import ru.skoltech.cedl.dataexchange.entity.tradespace.Optimality;
import ru.skoltech.cedl.dataexchange.entity.tradespace.TradespaceToStudyBridge;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.ui.control.fom.FigureOfMeritDefinitionDeleteCell;
import ru.skoltech.cedl.dataexchange.ui.control.fom.FigureOfMeritDefinitionOptimalityCell;
import ru.skoltech.cedl.dataexchange.ui.control.fom.FigureOfMeritDefinitionParameterLinkCell;
import ru.skoltech.cedl.dataexchange.ui.control.fom.FigureOfMeritDefinitionUnitCell;
import ru.skoltech.cedl.dataexchange.ui.utils.BeanPropertyCellValueFactory;

import java.net.URL;
import java.util.Comparator;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Controller for figures of merit editor.
 * <p>
 * Created by D.Knoll on 28.06.2017.
 */
public class FiguresOfMeritEditorController implements Initializable {

    private static final Logger logger = Logger.getLogger(FiguresOfMeritEditorController.class);

    @FXML
    private TableView<FigureOfMeritDefinition> figureOfMeritTable;
    @FXML
    private TableColumn<FigureOfMeritDefinition, String> nameColumn;
    @FXML
    private TableColumn<FigureOfMeritDefinition, FigureOfMeritDefinition> unitColumn;
    @FXML
    private TableColumn<FigureOfMeritDefinition, FigureOfMeritDefinition> optimalityColumn;
    @FXML
    private TableColumn<FigureOfMeritDefinition, FigureOfMeritDefinition> parameterLinkColumn;
    @FXML
    private TableColumn<FigureOfMeritDefinition, FigureOfMeritDefinition> deleteColumn;

    private GuiService guiService;
    private TradespaceToStudyBridge tradespaceToStudyBridge;
    private MultitemporalTradespace tradespace;

    private FiguresOfMeritEditorController() {
    }

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setTradespaceToStudyBridge(TradespaceToStudyBridge tradespaceToStudyBridge) {
        this.tradespaceToStudyBridge = tradespaceToStudyBridge;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        figureOfMeritTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Callback<CellDataFeatures<FigureOfMeritDefinition, FigureOfMeritDefinition>, ObservableValue<FigureOfMeritDefinition>> figureOfMeritCallback = param -> {
            if (param == null || param.getValue() == null) {
                return new SimpleObjectProperty<>();
            }
            FigureOfMeritDefinition fom = param.getValue();
            return new SimpleObjectProperty<>(fom);
        };
        Consumer<Void> updateFigureOfMeritTableConsumer = (v) -> figureOfMeritTable.refresh();

        nameColumn.setCellValueFactory(BeanPropertyCellValueFactory.createBeanPropertyCellValueFactory("name"));
        nameColumn.setStyle("-fx-alignment: BASELINE_LEFT;");

        unitColumn.setCellValueFactory(figureOfMeritCallback);
        unitColumn.setCellFactory(p -> new FigureOfMeritDefinitionUnitCell(tradespaceToStudyBridge, updateFigureOfMeritTableConsumer));

        optimalityColumn.setCellValueFactory(figureOfMeritCallback);
        optimalityColumn.setCellFactory(p -> new FigureOfMeritDefinitionOptimalityCell(updateFigureOfMeritTableConsumer));

        parameterLinkColumn.setCellValueFactory(figureOfMeritCallback);
        parameterLinkColumn.setCellFactory(p -> new FigureOfMeritDefinitionParameterLinkCell(guiService, tradespaceToStudyBridge, updateFigureOfMeritTableConsumer));

        deleteColumn.setCellValueFactory(figureOfMeritCallback);
        Consumer<FigureOfMeritDefinition> deleteConsumer = (fom) -> {
            tradespace.getDefinitions().remove(fom);
            figureOfMeritTable.getItems().remove(fom);
            // TODO: remove also data from design points?
            figureOfMeritTable.refresh();
        };
        deleteColumn.setCellFactory(p -> new FigureOfMeritDefinitionDeleteCell(deleteConsumer));
    }

    public void addFigureOfMerit() {
        Optional<String> figureOfMeritChoice = Dialogues.inputParameterName("figure of merit");
        if (figureOfMeritChoice.isPresent()) {
            String figureOfMeritName = figureOfMeritChoice.get();
            boolean hasSameName = tradespace.getDefinitionsMap().containsKey(figureOfMeritName);
            if (hasSameName) {
                Dialogues.showWarning("Duplicate figure of merit name", "Such a figure of merit was already defined!");
            } else {
                FigureOfMeritDefinition fom = new FigureOfMeritDefinition(figureOfMeritName, "none", Optimality.MAXIMAL); // TODO add unit
                tradespace.getDefinitions().add(fom);
                figureOfMeritTable.getItems().add(fom);
            }
        }
    }

    public void setTradespace(MultitemporalTradespace tradespace) {
        this.tradespace = tradespace;
        ObservableList<FigureOfMeritDefinition> figureOfMeritDefinitions = FXCollections.observableArrayList();
        if (tradespace != null) {
            figureOfMeritDefinitions.addAll(tradespace.getDefinitions());
            figureOfMeritDefinitions.sort(Comparator.naturalOrder());
        }
        figureOfMeritTable.setItems(figureOfMeritDefinitions);
    }
}


