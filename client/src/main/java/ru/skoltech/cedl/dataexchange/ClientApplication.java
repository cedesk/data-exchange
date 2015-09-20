package ru.skoltech.cedl.dataexchange;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import ru.skoltech.cedl.dataexchange.controller.MainController;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.view.Views;

import java.io.File;

public class ClientApplication extends Application {

    private static Logger logger = Logger.getLogger(ClientApplication.class);

    public static void main(String[] args) {
        if (System.getProperty("cedesk.data.dir") == null) {
            System.setProperty("cedesk.data.dir", new File(System.getProperty("user.home"), "CEDESK").getAbsolutePath());
        }
        // ensure log directory exists
        new File(System.getProperty("cedesk.data.dir")).mkdirs();
        // now it's safe to configure log4j
        PropertyConfigurator.configure(ClientApplication.class.getResource("/META-INF/log4j.properties"));

        logger.info("----------------------------------------------------------------------------------------------------");
        logger.info("Opening CEDESK ...");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Views.APPLICATION_VIEW);
        Parent root = loader.load();
        MainController mainController = loader.getController();

        primaryStage.setTitle("Concurrent Engineering Data Exchange Skoltech");
        primaryStage.setScene(new Scene(root, 900, 700));
        primaryStage.getIcons().add(IconSet.APP_ICON);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                logger.info("Closing CEDESK ...");
                mainController.terminate();
                logger.info("CEDESK terminated.");
            }
        });
    }
}