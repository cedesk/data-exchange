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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import ru.skoltech.cedl.dataexchange.StatusLogger;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static ru.skoltech.cedl.dataexchange.StatusLogger.LogType.INFO;
import static ru.skoltech.cedl.dataexchange.StatusLogger.LogType.WARN;

/**
 * Controller for the status bar log messages.
 * <p>
 * Created by Nikolay Groshkov on 11-Sep-17.
 */
public class StatusController implements Initializable {

    @FXML
    private TitledPane statusBarPane;
    @FXML
    private Label statusBarLabel;
    @FXML
    private TextArea statusBarTextArea;

    private StatusLogger statusLogger;

    public void setStatusLogger(StatusLogger statusLogger) {
        this.statusLogger = statusLogger;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusBarLabel.minWidthProperty().bind(statusBarPane.widthProperty().subtract(30));
        statusBarLabel.textProperty().bind(
                Bindings.concat("  ", Bindings.when(statusLogger.lastMessageProperty().isNotNull())
                        .then(statusLogger.lastMessageProperty()).otherwise("")));
        statusBarLabel.backgroundProperty().bind(
                Bindings.when(statusLogger.lastLogTypeProperty().isEqualTo(INFO))
                        .then(new Background(new BackgroundFill(Color.MINTCREAM, CornerRadii.EMPTY, Insets.EMPTY)))
                        .otherwise(Bindings.when(statusLogger.lastLogTypeProperty().isEqualTo(WARN))
                                .then(new Background(new BackgroundFill(Color.GOLD, CornerRadii.EMPTY, Insets.EMPTY)))
                                .otherwise(new Background(new BackgroundFill(Color.DARKORANGE, CornerRadii.EMPTY, Insets.EMPTY)))));

        statusBarTextArea.textProperty().bind(
                Bindings.createStringBinding(() -> statusLogger.messages().stream()
                        .map(pair -> "[" + pair.getRight().name() + "]: \t" + pair.getLeft())
                        .collect(Collectors.joining("\n")), statusLogger.lastMessageProperty()));

//        statusBarTextArea.textProperty()
//                .addListener((observable, oldValue, newValue) -> {
        // TODO: scroll to the end
        // statusBarTextArea.setScrollTop(Double.MAX_VALUE);
//                });
    }
}
