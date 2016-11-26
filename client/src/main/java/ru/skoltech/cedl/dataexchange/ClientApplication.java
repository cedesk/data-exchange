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
import ru.skoltech.cedl.dataexchange.repository.StorageUtils;
import ru.skoltech.cedl.dataexchange.structure.view.IconSet;
import ru.skoltech.cedl.dataexchange.view.Views;

public class ClientApplication extends Application {

    private static Logger logger = Logger.getLogger(ClientApplication.class);
    private MainController mainController;

    public static void main(String[] args) {
        System.out.println("using: " + StorageUtils.getAppDir().getAbsolutePath());
        PropertyConfigurator.configure(ClientApplication.class.getResource("/META-INF/log4j.properties"));

        logger.info("----------------------------------------------------------------------------------------------------");
        logger.info("Opening CEDESK ...");
        String appVersion = ApplicationProperties.getAppVersion();
        String dbSchemaVersion = ApplicationProperties.getDbSchemaVersion();
        logger.info("Application Version " + appVersion + ", DB Schema Version " + dbSchemaVersion);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Views.MAIN_WINDOW);
        Parent root = loader.load();
        mainController = loader.getController();

        primaryStage.setTitle("Concurrent Engineering Data Exchange Skoltech");
        primaryStage.setScene(new Scene(root));
        primaryStage.getIcons().add(IconSet.APP_ICON);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                if (!mainController.confirmCloseRequest()) {
                    we.consume();
                }
            }
        });
    }

    @Override
    public void stop() throws Exception {
        logger.info("Stopping CEDESK ...");
        try {
            mainController.terminate();
        } catch (Exception e) {
            logger.warn("", e);
        }
        logger.info("CEDESK stopped.");
    }
}