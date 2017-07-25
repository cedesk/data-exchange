/*
 * Copyright (C) 2017 Dominik Knoll and Nikolay Groshkov - All Rights Reserved
 * You may use, distribute and modify this code under the terms of the MIT license.
 *
 *  See file LICENSE.txt or go to https://opensource.org/licenses/MIT for full license details.
 */

package ru.skoltech.cedl.dataexchange.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.GuiUtils;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 23.06.2015.
 */
public class GuideController implements Initializable {

    private static final Logger logger = Logger.getLogger(GuideController.class);

    @FXML
    private WebView guideView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GuiUtils.loadWebView(guideView, getClass(), "guide.html");
    }
}
