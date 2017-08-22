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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.tradespace.FigureOfMeritDefinition;
import ru.skoltech.cedl.dataexchange.entity.tradespace.Optimality;
import ru.skoltech.cedl.dataexchange.entity.tradespace.TradespaceToStudyBridge;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.net.URL;
import java.util.Collection;
import java.util.EnumSet;
import java.util.ResourceBundle;

/**
 * Controller for figure of merit.
 * <p>
 * Created by D.Knoll on 24.09.2015.
 */
public class FigureOfMeritController implements Initializable {

    private static final Logger logger = Logger.getLogger(FigureOfMeritController.class);

    @FXML
    private TextField figureOfMeritNameText;
    @FXML
    private TextField unitOfMeasureText;
    @FXML
    private ChoiceBox<Optimality> optimalityChoice;
    @FXML
    private TextField parameterLinkText;

    private FigureOfMeritDefinition figureOfMeritDefinition;
    private TradespaceToStudyBridge tradespaceToStudyBridge;
    private GuiService guiService;

    public FigureOfMeritController() {
        super();
        figureOfMeritDefinition = new FigureOfMeritDefinition("figure of merit", "unit", Optimality.MAXIMAL);
    }

    public FigureOfMeritController(FigureOfMeritDefinition figureOfMeritDefinition) {
        this.figureOfMeritDefinition = figureOfMeritDefinition;
    }

    public FigureOfMeritDefinition getFigureOfMeritDefinition() {
        return figureOfMeritDefinition;
    }

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    public void setTradespaceModelBridge(TradespaceToStudyBridge tradespaceModelBridge) {
        this.tradespaceToStudyBridge = tradespaceModelBridge;
    }

    public void chooseParameter() {
        Collection<ParameterModel> parameters = tradespaceToStudyBridge.getModelOutputParameters();

        ViewBuilder parameterSelectorViewBuilder = guiService.createViewBuilder("Link Selector", Views.PARAMETER_SELECTOR_VIEW);
        parameterSelectorViewBuilder.applyEventHandler(event -> {
            ParameterModel parameterModel = (ParameterModel) event.getSource();
            if (parameterModel != null) {
                // update model
                figureOfMeritDefinition.setParameterModelLink(parameterModel.getUuid());
                // update view
                String parameterName = tradespaceToStudyBridge.getParameterName(figureOfMeritDefinition.getParameterModelLink());
                parameterLinkText.setText(parameterName);
                String parameterUnitOfMeasure = tradespaceToStudyBridge.getParameterUnitOfMeasure(figureOfMeritDefinition.getParameterModelLink());
                unitOfMeasureText.setText(parameterUnitOfMeasure);
                unitOfMeasureText.setEditable(false);
            }
        });
        parameterSelectorViewBuilder.showAndWait(parameters, null);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        optimalityChoice.setItems(FXCollections.observableArrayList(EnumSet.allOf(Optimality.class)));
        optimalityChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                figureOfMeritDefinition.setOptimality(newValue);
            }
        });
        unitOfMeasureText.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                figureOfMeritDefinition.setUnitOfMeasure(newValue);
            }
        });
        updateView();
    }

    private void updateView() {
        if (figureOfMeritDefinition != null) {
            figureOfMeritNameText.setText(figureOfMeritDefinition.getName());
            unitOfMeasureText.setText(figureOfMeritDefinition.getUnitOfMeasure());
            optimalityChoice.setValue(figureOfMeritDefinition.getOptimality());

            if (figureOfMeritDefinition.getParameterModelLink() != null) {
                String parameterName = tradespaceToStudyBridge.getParameterName(figureOfMeritDefinition.getParameterModelLink());
                parameterLinkText.setText(parameterName);
                String parameterUnitOfMeasure = tradespaceToStudyBridge.getParameterUnitOfMeasure(figureOfMeritDefinition.getParameterModelLink());
                unitOfMeasureText.setText(parameterUnitOfMeasure);
                unitOfMeasureText.setEditable(false);
            }
        }
    }

}
