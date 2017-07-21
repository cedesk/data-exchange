/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
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
