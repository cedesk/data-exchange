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

package ru.skoltech.cedl.dataexchange.service.impl;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.service.GuiService;
import ru.skoltech.cedl.dataexchange.service.ViewBuilder;
import ru.skoltech.cedl.dataexchange.ui.controller.FXMLLoaderFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Implementation of {@link GuiService}
 * <p>
 * Created by D.Knoll on 27.03.2017.
 */
public class GuiServiceImpl implements GuiService {

    private static final Logger logger = Logger.getLogger(GuiServiceImpl.class);

    private FXMLLoaderFactory fxmlLoaderFactory;
    private Locale locale;

    public void setFxmlLoaderFactory(FXMLLoaderFactory fxmlLoaderFactory) {
        this.fxmlLoaderFactory = fxmlLoaderFactory;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public void copyTextToClipboard(String code) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        HashMap<DataFormat, Object> content = new HashMap<>();
        content.put(DataFormat.PLAIN_TEXT, code);
        clipboard.setContent(content);
    }

    @Override
    public <T extends Node> T createControl(URL location, Object... args) {
        try {
            FXMLLoader loader = fxmlLoaderFactory.createFXMLLoader(location, args);
            loader.setResources(ResourceBundle.getBundle("i18n.MessagesBundle", locale));
            T control = loader.load();
            Object controller = loader.getController();
            control.setUserData(controller);
            return control;
        } catch (IOException e) {
            logger.error("Unable to load control: " + location, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends Node> T createControl(URL location) {
        return this.createControl(location, new Object[0]);
    }

    @Override
    public ViewBuilder createViewBuilder(String title, URL location) {
        return new ViewBuilder(fxmlLoaderFactory, locale, title, location);
    }

    @Override
    public String loadResourceContent(Class resourceClass, String filename) throws Exception {
        URL fileLocation = resourceClass.getResource(filename);
        String baseLocation = fileLocation.toExternalForm().replace(filename, "");
        try (InputStream in = fileLocation.openStream()) {
            String content = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
            content = content.replace("src=\"", "src=\"" + baseLocation);
            return content;
        } catch (IOException e) {
            logger.error("Error loading web content from resource", e);
            throw new Exception("Error loading web content from resource", e);
        }
    }
}
