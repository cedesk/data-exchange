package ru.skoltech.cedl.dataexchange;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
public class GuiUtils {

    private static final Logger logger = Logger.getLogger(GuiUtils.class);

    public static void openView(String title, URL viewLocation, Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(viewLocation);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.getIcons().add(IconSet.APP_ICON);
            stage.initModality(Modality.NONE);
            stage.initOwner(owner);

            stage.show();
        } catch (IOException e) {
            logger.error(e);
        }
    }

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

    public static void copyTextToClipboard(String code) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        HashMap<DataFormat, Object> content = new HashMap<>();
        content.put(DataFormat.PLAIN_TEXT, code);
        clipboard.setContent(content);
    }
}
