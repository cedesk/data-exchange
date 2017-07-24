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

package ru.skoltech.cedl.dataexchange;

import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.log4j.Logger;

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
public class GuiUtils {

    private static final Logger logger = Logger.getLogger(GuiUtils.class);

    public static void loadWebView(WebView guideView, Class resourceClass, String filename) {
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

    public static void copyTextToClipboard(String code) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        HashMap<DataFormat, Object> content = new HashMap<>();
        content.put(DataFormat.PLAIN_TEXT, code);
        clipboard.setContent(content);
    }
}
