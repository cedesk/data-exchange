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

package ru.skoltech.cedl.dataexchange.services.impl;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.controller.FXMLLoaderFactory;
import ru.skoltech.cedl.dataexchange.services.GuiService;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Created by D.Knoll on 27.03.2017.
 */
public class GuiServiceImpl implements GuiService {

    private static final Logger logger = Logger.getLogger(GuiServiceImpl.class);

    private FXMLLoaderFactory fxmlLoaderFactory;

    public void setFxmlLoaderFactory(FXMLLoaderFactory fxmlLoaderFactory) {
        this.fxmlLoaderFactory = fxmlLoaderFactory;
    }

    @Override
    public void openView(String title, URL location, Window ownerWindow) {
        this.openView(title, location, ownerWindow, Modality.NONE);
    }

    @Override
    public void openView(String title, URL location, Window ownerWindow, StageStartAction stageStartAction) {
        this.openView(title, location, ownerWindow, Modality.NONE, stageStartAction, new Object[0]);
    }

    @Override
    public void openView(String title, URL location, Window ownerWindow, Object... args) {
        this.openView(title, location, ownerWindow, Modality.NONE, args);
    }

    @Override
    public void openView(String title, URL location, Window ownerWindow, Modality modality) {
        this.openView(title, location, ownerWindow, modality, new Object[0]);
    }

    @Override
    public void openView(String title, URL location, Window ownerWindow, Modality modality, StageStartAction stageStartAction) {
        this.openView(title, location, ownerWindow, modality, stageStartAction, new Object[0]);
    }

    @Override
    public void openView(String title, URL location, Window ownerWindow, Modality modality, Object... args) {
        this.openView(title, location, ownerWindow, modality, StageStartAction.SHOW, new Object[0]);
    }

    @Override
    public void openView(String title, URL location, Window ownerWindow, Modality modality, StageStartAction stageStartAction, Object... args) {
        try {
            FXMLLoader loader = fxmlLoaderFactory.createFXMLLoader(location, args);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(modality);
            stage.initOwner(ownerWindow);
            if (stageStartAction == StageStartAction.SHOW_AND_WAIT) {
                stage.showAndWait();
            } else {
                stage.show();
            }
        } catch (IOException e) {
            logger.error(e);
        }
    }


    @Override
    public void loadWebView(WebView guideView, Class resourceClass, String filename) {
        URL fileLocation = resourceClass.getResource(filename);
        String baseLocation = fileLocation.toExternalForm().replace(filename, "");
        String content = "";
        try (InputStream in = fileLocation.openStream()) {
            content = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            logger.error("Error loading web content from resource", e);
        }
        WebEngine webEngine = guideView.getEngine();
        webEngine.getLoadWorker().exceptionProperty().addListener((observableValue, oldThrowable, newThrowable) ->
                logger.error("Load exception ", newThrowable));
        content = content.replace("src=\"", "src=\"" + baseLocation);

        webEngine.loadContent(content);
    }

    @Override
    public void copyTextToClipboard(String code) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        HashMap<DataFormat, Object> content = new HashMap<>();
        content.put(DataFormat.PLAIN_TEXT, code);
        clipboard.setContent(content);
    }
}
