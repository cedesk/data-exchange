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

package ru.skoltech.cedl.dataexchange.control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import ru.skoltech.cedl.dataexchange.controller.Dialogues;
import ru.skoltech.cedl.dataexchange.entity.tradespace.FigureOfMeritDefinition;
import ru.skoltech.cedl.dataexchange.entity.tradespace.MultitemporalTradespace;
import ru.skoltech.cedl.dataexchange.entity.tradespace.Optimality;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 28.06.2017.
 */
public class FiguresOfMeritEditor extends ScrollPane implements Initializable {

    private static final Logger logger = Logger.getLogger(FiguresOfMeritEditor.class);

    private MultitemporalTradespace tradespace;

    @FXML
    private VBox figuresOfMeritsViewContainer;

    public FiguresOfMeritEditor() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("figures_of_merit_editor.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public MultitemporalTradespace getTradespace() {
        return tradespace;
    }

    public void setTradespace(MultitemporalTradespace tradespace) {
        this.tradespace = tradespace;
        updateView();
    }

    public void addFigureOfMerit(ActionEvent actionEvent) {
        Optional<String> figureOfMeritChoice = Dialogues.inputParameterName("figure of merit");
        if (figureOfMeritChoice.isPresent()) {
            String figureOfMeritName = figureOfMeritChoice.get();
            boolean hasSameName = tradespace.getDefinitionsMap().containsKey(figureOfMeritName);
            if (hasSameName) {
                Dialogues.showWarning("Duplicate figure of merit name", "Such a figure of merit was already defined!");
            } else {
                FigureOfMeritDefinition fom = new FigureOfMeritDefinition(figureOfMeritName, "none", Optimality.MAXIMAL); // TODO add unit
                tradespace.getDefinitions().add(fom);
                renderFigureOfMerit(fom);
            }
        }
    }

    public void deleteFigureOfMerit(ActionEvent actionEvent) {
        Button deleteButton = (Button) actionEvent.getSource();
        HBox argumentRow = (HBox) deleteButton.getUserData();
        FigureOfMeritView viewer = (FigureOfMeritView) argumentRow.getChildren().get(0);
        FigureOfMeritDefinition fomDef = viewer.getFigureOfMeritDefinition();
        Optional<ButtonType> yesNo = Dialogues.chooseYesNo("Delete Figure of Merit", "Are you sure you want to delete the figure of merit '" + fomDef.getName() + "'?");
        if (yesNo.isPresent() && yesNo.get() == ButtonType.YES) {
            tradespace.getDefinitions().remove(fomDef); // TODO: remove also data from design points?
            figuresOfMeritsViewContainer.getChildren().remove(argumentRow);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void reloadValues(ActionEvent actionEvent) {
        // update FOMvalues from Model
    }

    private void renderFigureOfMerit(FigureOfMeritDefinition figureOfMeritDefinition) {
        FigureOfMeritView editor = new FigureOfMeritView(figureOfMeritDefinition);
        Button removeButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.MINUS));
        removeButton.setTooltip(new Tooltip("Remove figure of merit"));
        removeButton.setOnAction(FiguresOfMeritEditor.this::deleteFigureOfMerit);
        removeButton.setMinWidth(28);
        HBox extModRow = new HBox(6, editor, removeButton);
        removeButton.setUserData(extModRow);
        figuresOfMeritsViewContainer.getChildren().add(extModRow);
    }

    private void updateView() {
        if (tradespace != null) {
            List<FigureOfMeritDefinition> figureOfMeritDefinitions = tradespace.getDefinitions();
            for (FigureOfMeritDefinition figureOfMeritDefinition : figureOfMeritDefinitions) {
                renderFigureOfMerit(figureOfMeritDefinition);
            }
        }
    }
}


