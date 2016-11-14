package ru.skoltech.cedl.dataexchange.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebView;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 23.06.2015.
 */
public class AboutController implements Initializable {

    private static final Logger logger = Logger.getLogger(AboutController.class);

    @FXML
    private WebView contentView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MainController.loadWebView(contentView, getClass(), "about.html");
    }
}
