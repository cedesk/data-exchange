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

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.skoltech.cedl.dataexchange.structure.Project;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for component library window.
 * <p>
 * Created by Dominik Knoll on 06-Nov-17.
 */
public class LibraryController implements Initializable, Displayable {

    @FXML
    private TextField searchTextField;

    @FXML
    private ChoiceBox categoryChoice;
    @FXML
    private TextField keywordText;

    private Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public void display(Stage stage, WindowEvent windowEvent) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
