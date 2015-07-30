package ru.skoltech.cedl.dataexchange.controller;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.controlsfx.control.HyperlinkLabel;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by D.Knoll on 23.06.2015.
 */
public class AboutController implements Initializable {

    public Label versionInfoLabel;
    public HyperlinkLabel authorsInfoLabel;
    public HyperlinkLabel librariesInfoLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String version = AboutController.class.getPackage().getImplementationVersion();
        versionInfoLabel.setText(version);
        authorsInfoLabel.setText("Dominik J. Knoll [d.knoll@skoltech.ru]");
        librariesInfoLabel.setText("Hibernate,\nMySQL,\nslf4j,\nApache POI, Apache Commons Math 3,\nControlsFX - FX Experience,\nJFXtras");
    }
}
