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

package ru.skoltech.cedl.dataexchange.ui.control.tradespace;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.controlsfx.glyphfont.Glyph;
import ru.skoltech.cedl.dataexchange.entity.ParameterModel;
import ru.skoltech.cedl.dataexchange.entity.tradespace.FigureOfMeritDefinition;
import ru.skoltech.cedl.dataexchange.entity.tradespace.TradespaceToStudyBridge;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.ui.Views;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * {@link javafx.scene.control.TableView} cell to display {@link FigureOfMeritDefinition} parameter link.
 */
public class FigureOfMeritDefinitionParameterLinkCell extends TableCell<FigureOfMeritDefinition, FigureOfMeritDefinition> {

    private final TradespaceToStudyBridge tradespaceToStudyBridge;
    private FigureOfMeritDefinition fom;
    private final TextField parameterLinkTextField = new TextField();
    private BorderPane parameterLinkBorderPane = new BorderPane();

    public FigureOfMeritDefinitionParameterLinkCell(GuiService guiService, TradespaceToStudyBridge tradespaceToStudyBridge, Consumer<Void> parameterNameSupplier) {
        this.tradespaceToStudyBridge = tradespaceToStudyBridge;
        Button chooseParameterButton = new Button();
        parameterLinkTextField.setEditable(false);
        parameterLinkBorderPane.setCenter(parameterLinkTextField);
        parameterLinkBorderPane.setRight(chooseParameterButton);
        BorderPane.setMargin(chooseParameterButton, new Insets(0, 0, 0, 3));
        chooseParameterButton.setGraphic(new Glyph("FontAwesome", "LINK"));
        chooseParameterButton.setOnAction(t -> {
            Collection<ParameterModel> parameters = tradespaceToStudyBridge.getModelOutputParameters();
            ViewBuilder parameterSelectorViewBuilder = guiService.createViewBuilder("Link Selector", Views.PARAMETER_SELECTOR_VIEW);
            parameterSelectorViewBuilder.applyEventHandler(event -> {
                ParameterModel parameterModel = (ParameterModel) event.getSource();
                if (parameterModel != null && fom != null) {
                    // update model
                    fom.setParameterModelLink(parameterModel.getUuid());
                    // update view
                    String parameterName = tradespaceToStudyBridge.getParameterName(fom.getParameterModelLink());
                    parameterLinkTextField.setText(parameterName);
                    parameterNameSupplier.accept(null);
                }
            });
            parameterSelectorViewBuilder.showAndWait(parameters, null);
        });
    }

    @Override
    protected void updateItem(FigureOfMeritDefinition item, boolean empty) {
        super.updateItem(item, empty);
        fom = !empty ? item : null;
        if (!empty) {
            String parameterName = tradespaceToStudyBridge.getParameterName(fom.getParameterModelLink());
            parameterLinkTextField.setText(parameterName);
            this.setGraphic(parameterLinkBorderPane);
            this.setAlignment(Pos.BASELINE_CENTER);
        }
    }
}