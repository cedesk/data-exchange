package ru.skoltech.cedl.dataexchange.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 23.06.2015.
 */
public class AboutController implements Initializable {

    @FXML
    private WebView contentView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        URL url = getClass().getResource("/about.html");
        WebEngine webEngine = contentView.getEngine();
        webEngine.load(url.toExternalForm());
    }
}
