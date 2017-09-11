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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.service.GuiService;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for about window.
 * <p>
 * Created by D.Knoll on 23.06.2015.
 */
public class AboutController implements Initializable {

    private static final Logger logger = Logger.getLogger(AboutController.class);
    @FXML
    public AnchorPane aboutPane;
    @FXML
    private WebView contentView;

    private GuiService guiService;

    public void setGuiService(GuiService guiService) {
        this.guiService = guiService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        guiService.loadWebView(contentView, getClass(), "about.html");
    }
}
