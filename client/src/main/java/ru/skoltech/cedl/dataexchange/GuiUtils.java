package ru.skoltech.cedl.dataexchange;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.log4j.Logger;
import ru.skoltech.cedl.dataexchange.controller.MainController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
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
        webEngine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {
            @Override
            public void changed(ObservableValue<? extends Throwable> observableValue, Throwable oldThrowable, Throwable newThrowable) {
                logger.error("Load exception ", newThrowable);
            }
        });
        content = content.replace("src=\"", "src=\"" + baseLocation);

        webEngine.loadContent(content);
    }
}
