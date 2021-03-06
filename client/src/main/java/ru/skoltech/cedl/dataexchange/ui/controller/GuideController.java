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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.service.GuiService;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 23.06.2015.
 */
public class GuideController implements Initializable {

    private static final Logger logger = Logger.getLogger(GuideController.class);

    @FXML
    private WebView guideView;

    private GuiService guiService;

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            URL guideLocation = GuideController.class.getResource("guide.html");
            String content = guiService.loadResourceContent(guideLocation);
            WebEngine webEngine = guideView.getEngine();
            webEngine.loadContent(content);
        } catch (Exception e) {
            logger.error("Cannot load 'guide' page: " + e.getMessage(), e);
        }
    }
}
