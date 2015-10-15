package ru.skoltech.cedl.dataexchange.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
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
        URI uri = null;
        try {
            uri = getClass().getResource("/about.html").toURI();
        } catch (URISyntaxException e) {
            logger.error(e);
        }
        WebEngine webEngine = contentView.getEngine();
        webEngine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
            @Override
            public void changed(ObservableValue<? extends Throwable> observableValue, Throwable oldThrowable, Throwable newThrowable) {
                logger.error("Load exception ", newThrowable);
            }
        });
        webEngine.load(uri.toString());
    }
}
