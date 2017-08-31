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
import javafx.scene.control.TableView;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.entity.Study;
import ru.skoltech.cedl.dataexchange.external.ExternalModelFileHandler;
import ru.skoltech.cedl.dataexchange.structure.Project;
import ru.skoltech.cedl.dataexchange.structure.analytics.ModelInconsistency;
import ru.skoltech.cedl.dataexchange.structure.analytics.ParameterLinkRegistry;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for consistency validation window.
 * <p>
 * Created by D.Knoll on 26.06.2017.
 */
public class ConsistencyController implements Initializable {

    private static final Logger logger = Logger.getLogger(ConsistencyController.class);

    @FXML
    private TableView<ModelInconsistency> inconsistenciesTable;

    private Project project;
    private ExternalModelFileHandler externalModelFileHandler;
    private ParameterLinkRegistry parameterLinkRegistry;

    public void setProject(Project project) {
        this.project = project;
    }

    public void setExternalModelFileHandler(ExternalModelFileHandler externalModelFileHandler) {
        this.externalModelFileHandler = externalModelFileHandler;
    }
    public void setParameterLinkRegistry(ParameterLinkRegistry parameterLinkRegistry) {
        this.parameterLinkRegistry = parameterLinkRegistry;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshView();
    }

    public void refreshView() {
        Study localStudy = project.getStudy();
        inconsistenciesTable.getItems().clear();
        List<ModelInconsistency> modelInconsistencies = ModelInconsistency.analyzeModel(externalModelFileHandler, parameterLinkRegistry, localStudy);
        inconsistenciesTable.getItems().addAll(modelInconsistencies);
    }

}
